package org.example.search.owl;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.semanticweb.owlapi.util.PriorityCollection;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class OwlHelper implements AutoCloseable {
    private final OWLOntologyManager ontologyManager;
    private OWLOntology owlOntology;
    private OWLDataFactory owlDataFactory;
    private final String prefix;

    public OwlHelper(final Path owlFile) throws OwlException {
        ontologyManager = OWLManager.createOWLOntologyManager();
        final PriorityCollection<OWLOntologyIRIMapper> iriMappers = ontologyManager.getIRIMappers();
        iriMappers.add(new AutoIRIMapper(owlFile.toFile(), true));
        try {
            owlOntology = ontologyManager.loadOntologyFromOntologyDocument(owlFile.toFile());
        } catch (OWLException e) {
            throw new OwlException(e);
        }
        owlDataFactory = ontologyManager.getOWLDataFactory();
        OWLDocumentFormat format = ontologyManager.getOntologyFormat(owlOntology);
        prefix = Optional.ofNullable(format)
                .filter(OWLDocumentFormat::isPrefixOWLDocumentFormat)
                .map(ontologyFormat -> ontologyFormat.asPrefixOWLDocumentFormat().getDefaultPrefix())
                .orElse("").concat("#");
    }

    private String getReminder(final IRI iri) {
        return iri.toString().replaceFirst(prefix, "");
    }

    public void close() throws OwlException {
        try {
            ontologyManager.clearOntologies();
            owlOntology = ontologyManager.createOntology();
            owlDataFactory = ontologyManager.getOWLDataFactory();
        } catch (OWLOntologyCreationException e) {
            throw new OwlException(e);
        }
    }

    public List<String> getClasses() {
        return owlOntology.axioms(AxiomType.DECLARATION)
                .filter(axiom -> axiom.getEntity().isOWLClass())
                .map(axiom -> getReminder(axiom.getEntity().getIRI()))
                .collect(Collectors.toList());
    }

    public List<String> getIndividuals() {
        return owlOntology.axioms(AxiomType.DECLARATION)
                .filter(axiom -> axiom.getEntity().isOWLNamedIndividual())
                .map(axiom -> getReminder(axiom.getEntity().getIRI()))
                .collect(Collectors.toList());
    }

    public List<String> getIndividualsByClass(final String className) {
        return owlOntology.axioms(AxiomType.CLASS_ASSERTION)
                .filter(axiom -> Objects.equals(getReminder(axiom.getClassExpression().asOWLClass().getIRI()), className))
                .map(axiom -> getReminder(axiom.getIndividual().asOWLNamedIndividual().getIRI()))
                .collect(Collectors.toList());
    }

    public List<String> getObjectProperties() {
        return owlOntology.axioms(AxiomType.DECLARATION)
                .filter(axiom -> axiom.getEntity().isOWLObjectProperty())
                .map(axiom -> getReminder(axiom.getEntity().getIRI()))
                .collect(Collectors.toList());
    }

    public List<String> getObjectPropertyAssertionRangesByDomain(final String propertyName,
                                                                 final String individualDomainName) {
        return owlOntology.axioms(AxiomType.OBJECT_PROPERTY_ASSERTION)
                .filter(axiom -> Objects.equals(getReminder(axiom.getProperty().asOWLObjectProperty().getIRI()), propertyName)
                        && Objects.equals(getReminder(axiom.getSubject().asOWLNamedIndividual().getIRI()), individualDomainName))
                .map(axiom -> getReminder(axiom.getObject().asOWLNamedIndividual().getIRI()))
                .collect(Collectors.toList());
    }
}
