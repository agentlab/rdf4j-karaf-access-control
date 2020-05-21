package ru.agentlab.rdf4j.ppo.triplestore;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.rdf4j.model.IRI;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.agentlab.rdf4j.ppo.policies.PPManager;

public class FakeTripleStore implements TripleStore {
	private static Logger log = LoggerFactory.getLogger(FakeTripleStore.class);

	protected String superUser;
	protected IRI superUserIri;
	protected String anonymous;
	protected IRI anonymousIri;
	protected AccessControlSailRepository repository;
	protected PPManager ppManager;

	protected Repository unfilteredRepo;
	protected MemoryStore unfilteredStore;
	protected RepositoryConnection unfilteredConnection;

	public FakeTripleStore(PPManager ppManager, String superUser, String anonymous) {
		this.ppManager = ppManager;
		this.superUser = superUser;
		this.anonymous = anonymous;
		unfilteredStore = new MemoryStore();
		unfilteredRepo = new SailRepository(unfilteredStore);
		try {
			unfilteredRepo.init();
		    unfilteredConnection = unfilteredRepo.getConnection();
		    this.superUserIri = unfilteredConnection.getValueFactory().createIRI(superUser);
			this.anonymousIri = unfilteredConnection.getValueFactory().createIRI(anonymous);
		    log.info("Initialize store with {} triples", unfilteredConnection.size() - unfilteredConnection.size(unfilteredConnection.getValueFactory().createIRI(ppManager.getPoliciesContext())));
		} catch(RepositoryException e) {
		    log.error("Error initializy the fake triple store: {}", e.getMessage());
		}
	}

	public void init() {
		repository = new AccessControlSailRepository(new AccessControlSailImpl(unfilteredStore, unfilteredRepo, ppManager));
	}

	@Override
	public InterceptingRepositoryConnection getConnection(IRI webid){
		return repository.getConnection(webid);
	}

	public InterceptingRepositoryConnection getConnection(String webid){
		IRI webidIri = unfilteredConnection.getValueFactory().createIRI(webid);
		return repository.getConnection(webidIri);
	}

	// TODO maybe shift this logic to AccessControlSailRepository
	@Override
	public InterceptingRepositoryConnection getConnection() {
		return getConnection(anonymousIri);
	}

	@Override
	public InterceptingRepositoryConnection getSuperUserConnection() {
		return getConnection(superUserIri);
	}

	public RepositoryConnection getUnfilteredConnection() {
		return unfilteredRepo.getConnection();
	}

	@Override
	public long size() throws RepositoryException {
		RepositoryConnection connection = getSuperUserConnection();
		long size = connection.size() - connection.size(connection.getValueFactory().createIRI(ppManager.getPoliciesContext()));
		connection.close();
		return size;
	}

	public IRI getSuperUserIri() {
		return superUserIri;
	}

	public IRI getAnonymousIri() {
		return anonymousIri;
	}

	public void loadData(String file) {
		try {
			loadFile(unfilteredConnection, file, RDFFormat.TURTLE);
			log.info("Initialize store with {} triples", unfilteredConnection.size() - unfilteredConnection.size(unfilteredConnection.getValueFactory().createIRI(ppManager.getPoliciesContext())));
		} catch (Exception e) {
			log.error("Error reading data file: {}", e.getMessage());
			e.printStackTrace();
		}
	}

	public void loadPolicies(String file) {
		try {
			loadFile(unfilteredConnection, file, RDFFormat.TURTLE, ppManager.getPoliciesContext());
			log.info("Load {} ploicies triples", unfilteredConnection.size(unfilteredConnection.getValueFactory().createIRI(ppManager.getPoliciesContext())));
		} catch (Exception e) {
			log.error("Error reading policies file: {}", e.getMessage());
			e.printStackTrace();
		}
	}

	//for files with explicit context
	private void loadFile(RepositoryConnection connection, String file, RDFFormat format, String context) throws IOException, RDFParseException {
		try {
			ValueFactory valueFactory = connection.getValueFactory();
			IRI ctx = valueFactory.createIRI(context);
			InputStream in = this.getClass().getClassLoader().getResourceAsStream(file);
			connection.add(in, context, format, ctx);
			connection.commit();
		} catch(RepositoryException e) {
		    log.error("Error loading '': {}", file, e.getMessage());
		}
	}

	//for files with implicit contexts (e.g. TriG)
	private void loadFile(RepositoryConnection connection, String file, RDFFormat format) throws IOException, RDFParseException {
		try {
			InputStream in = this.getClass().getClassLoader().getResourceAsStream(file);
			connection.add(in, "http://example.org", format);
			connection.commit();
		} catch(RepositoryException e) {
		    log.error("Error loading '': {}", file, e.getMessage());
		}
	}
}
