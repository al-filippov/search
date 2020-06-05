package org.example.search.owl;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredDataPropertyCharacteristicAxiomGenerator;
import org.semanticweb.owlapi.util.InferredDisjointClassesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentDataPropertiesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentObjectPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.InferredIndividualAxiomGenerator;
import org.semanticweb.owlapi.util.InferredInverseObjectPropertiesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredObjectPropertyCharacteristicAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubDataPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubObjectPropertyAxiomGenerator;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OwlService {
    private OWLOntology ontology = null;
    private OWLOntologyManager man = null;
    private String AvtoSystemIRIShortFormString = "Автоматизированная_система";
    private String objectPropertyAStoDocString = "Описывается_в_документе";
    private String objectPropertyTermString = "Документ_связан_с_термином";
    private String objectPropertyTermToTermString = "Термин_связан_с_термином";
    private StructuralReasonerFactory factory = new StructuralReasonerFactory();
    private OWLReasoner hermit = null;

    private Stream<OWLNamedIndividual> getIndividualsByIRI(IRI iri) {
        return ontology.individualsInSignature();
    }

    private Stream<OWLClass> getClassesByIRI(IRI iri) {
        return ontology.classesInSignature();
    }

    private OWLObjectProperty getObjectPropertyByName(String name) {
        Stream<OWLObjectProperty> op = ontology.objectPropertiesInSignature();
        return op.filter(s -> s.getIRI().getShortForm().compareTo(name) == 0).findFirst().orElseThrow();
    }

    private OWLClass getSomeClassByIRI(IRI iri) {
        return ontology.classesInSignature().findFirst().orElse(null);
    }

    private void reasonerCheck() {
        try {
            StructuralReasonerFactory factory = new StructuralReasonerFactory();
            OWLReasoner reasoner = factory.createReasoner(ontology);
            reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS, InferenceType.OBJECT_PROPERTY_ASSERTIONS);

            reasoner.precomputeInferences();
            boolean consistent = reasoner.isConsistent();
            System.out.println("Consistent: " + consistent);
            System.out.println("\n");
            Node<OWLClass> bottomNode = reasoner.getUnsatisfiableClasses();
            Set<OWLClass> unsatisfiable = bottomNode.getEntitiesMinusBottom();
            if (!unsatisfiable.isEmpty()) {
                System.out.println("The following classes are unsatisfiable: ");
                for (OWLClass cls : unsatisfiable) {
                    System.out.println("    " + cls);
                }
            } else {
                System.out.println("There are no unsatisfiable classes");
            }
            System.out.println("\n");
        } catch (UnsupportedOperationException exception) {
            System.out.println("Unsupported reasoner operation.");
        } catch (Exception e) {
            System.out.println("Could not load the wine ontology: " + e.getMessage());
        }

    }

    private Stream<OWLNamedIndividual> ReasonerInstansesByClass(OWLClass owlClass) {
        Stream<OWLNamedIndividual> individuals = null;
        hermit.precomputeInferences(InferenceType.CLASS_ASSERTIONS, InferenceType.OBJECT_PROPERTY_ASSERTIONS);
        Stream<OWLClass> res = hermit.disjointClasses(owlClass);
        individuals = hermit.getInstances(owlClass, true).entities();
        return individuals;
    }

    private List<OWLNamedIndividual> ReasonerGetIndividualsByObjectProperty(OWLNamedIndividual owlNamedIndividual, OWLObjectProperty owlObjectProperty) {
        Stream<OWLNamedIndividual> individuals = null;
        StructuralReasonerFactory factory = new StructuralReasonerFactory();
        individuals = hermit.getObjectPropertyValues(owlNamedIndividual, owlObjectProperty).entities();
        return individuals.collect(Collectors.toList());
    }

    private OWLNamedIndividual getClassByIRI(String AvtoSystemIRIString, String userSearch) {
        Stream<OWLClass> classStream = ontology.classesInSignature();
        classStream = classStream.filter(s -> s.getIRI().
                getShortForm().compareTo(AvtoSystemIRIShortFormString) == 0);
        OWLClass owlClass = classStream.findFirst().get();
        Stream<OWLNamedIndividual> individualStream = ReasonerInstansesByClass(owlClass);
        // Получить экземпляр Марс_1С
        OWLNamedIndividual owlMarsAS = individualStream.filter(s -> s.getIRI().getShortForm().compareTo(userSearch) == 0).findFirst().get();
        return owlMarsAS;

    }

    private List<OWLNamedIndividual> getTermsBySystem(OWLNamedIndividual searchItem) {
        OWLObjectProperty objectPropertyDoc =
                getObjectPropertyByName(objectPropertyAStoDocString);
        // Получение индивидов документов по концепту системы с помощью МЛВ
        // МЛВ используется на случай эквивалентности терминов. Например документ
        // связан только с одним термином из нескольких эквивалентных
        List<OWLNamedIndividual> owlNamedIndividualsDocs = ReasonerGetIndividualsByObjectProperty(searchItem, objectPropertyDoc);
        //Получить ObjectProperty по названию. Получаем OP связывающую
        // концепт термина предметной области с концептами документов
        OWLObjectProperty objectPropertyTerm = getObjectPropertyByName(objectPropertyTermString);
        List<OWLNamedIndividual> owlNamedIndividualTerm = null;
        for (OWLNamedIndividual owlNamedIndividual : owlNamedIndividualsDocs
        ) {
            System.out.println("  " + owlNamedIndividual.getIRI().getShortForm());
            System.out.println("      Выберем термины которые употребляются в документе : ");
            owlNamedIndividualTerm = ReasonerGetIndividualsByObjectProperty(owlNamedIndividual, objectPropertyTerm);
            owlNamedIndividualTerm.forEach(s -> System.out.println(s.getIRI().getShortForm()));
        }
        return owlNamedIndividualTerm;
    }

    private List<OWLNamedIndividual> getTermsBySystem(OWLNamedIndividual termItem, OWLObjectProperty objectPropertyTermToTerm) {
        System.out.println("  Термины которые связаны с " + termItem.getIRI().getShortForm() + " : ");
        List<OWLNamedIndividual> termsByterm = ReasonerGetIndividualsByObjectProperty(termItem, objectPropertyTermToTerm);
        termsByterm.forEach(s -> System.out.println("    " + s.getIRI().getShortForm()));
        return termsByterm;
    }

    private void ReasonerSubClasses(OWLClass owlClass) {
        try {
            StructuralReasonerFactory factory = new StructuralReasonerFactory();
            Configuration config = new Configuration();
            config.tableauMonitorType = Configuration.TableauMonitorType.TIMING;
            Reasoner hermit = new Reasoner(config, ontology);
            System.out.println(hermit.isConsistent());
            System.out.println(owlClass);
            NodeSet<OWLClass> subClasses = hermit.getSubClasses(owlClass, true);
            subClasses.entities().forEach(System.out::println);
            NodeSet<OWLNamedIndividual> individuals = hermit.getInstances(owlClass);
            individuals.entities().forEach(System.out::println);
        } catch (UnsupportedOperationException exception) {
            System.out.println("Unsupported reasoner operation.");
        } catch (Exception e) {
            System.out.println("Could not load the wine ontology: " + e.getMessage());
        }
    }


    public void main() throws OWLOntologyCreationException {
        InputStream in = null;
        try {
            in = new FileInputStream(new File("ontology/Masha.owl"));
        } catch (FileNotFoundException ex) {
            System.out.println("Нет файла!");
        }
        man = OWLManager.createOWLOntologyManager();
        ontology = man.loadOntologyFromOntologyDocument(in);
        OWLDataFactory df = man.getOWLDataFactory();
        IRI documentIRI = man.getOntologyDocumentIRI(ontology);
        // System.out.println(documentIRI);

        hermit = factory.createReasoner(ontology);
        hermit.precomputeInferences(InferenceType.CLASS_ASSERTIONS, InferenceType.OBJECT_PROPERTY_ASSERTIONS);
        boolean consistencyCheck = hermit.isConsistent();
        if (consistencyCheck) {
            hermit.precomputeInferences(InferenceType.CLASS_HIERARCHY,
                    InferenceType.CLASS_ASSERTIONS, InferenceType.OBJECT_PROPERTY_HIERARCHY,
                    InferenceType.DATA_PROPERTY_HIERARCHY, InferenceType.OBJECT_PROPERTY_ASSERTIONS);

            List<InferredAxiomGenerator<? extends OWLAxiom>> generators = new ArrayList<>();
            generators.add(new InferredSubClassAxiomGenerator());
            generators.add(new InferredClassAssertionAxiomGenerator());
            generators.add(new InferredDataPropertyCharacteristicAxiomGenerator());
            generators.add(new InferredEquivalentClassAxiomGenerator());
            generators.add(new InferredEquivalentDataPropertiesAxiomGenerator());
            generators.add(new InferredEquivalentObjectPropertyAxiomGenerator());
            generators.add(new InferredInverseObjectPropertiesAxiomGenerator());
            generators.add(new InferredObjectPropertyCharacteristicAxiomGenerator());


            generators.add(new InferredSubClassAxiomGenerator());
            generators.add(new InferredSubDataPropertyAxiomGenerator());
            generators.add(new InferredSubObjectPropertyAxiomGenerator());
            List<InferredIndividualAxiomGenerator<? extends OWLIndividualAxiom>> individualAxioms =
                    new ArrayList<>();
            generators.addAll(individualAxioms);

            generators.add(new InferredDisjointClassesAxiomGenerator());
            InferredOntologyGenerator iog = new InferredOntologyGenerator(hermit, generators);

            OWLOntology inferredAxiomsOntology = man.createOntology();
            ontology = inferredAxiomsOntology;
            iog.fillOntology(df, inferredAxiomsOntology);
            File inferredOntologyFile = new File("output.txt");
            // Now we create a stream since the ontology manager can then write to that stream.
            try (OutputStream outputStream = new FileOutputStream(inferredOntologyFile)) {
                // We use the same format as for the input ontology.
                man.saveOntology(inferredAxiomsOntology, outputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (OWLOntologyStorageException e) {
                e.printStackTrace();
            }
        }
        Stream<OWLClass> classStream = getClassesByIRI(documentIRI);

        // Получить класс автоматизированная система
        classStream = classStream.filter(s -> s.getIRI().getShortForm().compareTo(AvtoSystemIRIShortFormString) == 0);
        OWLClass owlClass = classStream.findFirst().get();
        System.out.println();

        System.out.println("Пример №1 поиск терминов по АС через связанные с ней документы ");
        // Получить экземпляры класса автоматизорованная система
        Stream<OWLNamedIndividual> individualStream = ReasonerInstansesByClass(owlClass);

        // Получить экземпляр Марс_1С
        OWLNamedIndividual owlMasrAS = individualStream.findFirst().get();

        System.out.println("Выберем документы в которых описывается автоматизированная система : " +
                owlClass.getIRI().getShortForm() + " / " + owlMasrAS.getIRI().getShortForm());
        //Получить ObjectProperty по названию

        OWLObjectProperty objectPropertyTerm = getObjectPropertyByName(objectPropertyTermString);
        OWLObjectProperty objectPropertyDoc = getObjectPropertyByName(objectPropertyAStoDocString);
        //System.out.println(objectPropertyDoc.getIRI().getShortForm());
        List<OWLNamedIndividual> owlNamedIndividualsDocs = ReasonerGetIndividualsByObjectProperty(owlMasrAS, objectPropertyDoc);


        List<OWLNamedIndividual> owlNamedIndividualTerm = null;
        for (OWLNamedIndividual owlNamedIndividual : owlNamedIndividualsDocs
        ) {
            System.out.println("  " + owlNamedIndividual.getIRI().getShortForm());
            System.out.println("      Выберем термины которые употребляются в документе : ");
            owlNamedIndividualTerm = ReasonerGetIndividualsByObjectProperty(owlNamedIndividual, objectPropertyTerm);
            owlNamedIndividualTerm.forEach(s -> System.out.println(s.getIRI().getShortForm()));
        }

        OWLObjectProperty objectPropertyTermToTerm = getObjectPropertyByName(objectPropertyTermToTermString);
        OWLNamedIndividual term = owlNamedIndividualTerm.get(0);

        System.out.println("Пример №2 поиск терминов связаных с термином " + term.getIRI().getShortForm());
        System.out.println("  Термины которые связаны с " + term.getIRI().getShortForm() + " : ");
        List<OWLNamedIndividual> termsByterm = ReasonerGetIndividualsByObjectProperty(term, objectPropertyTermToTerm);
        termsByterm.forEach(s -> System.out.println("    " + s.getIRI().getShortForm()));
    }
}
