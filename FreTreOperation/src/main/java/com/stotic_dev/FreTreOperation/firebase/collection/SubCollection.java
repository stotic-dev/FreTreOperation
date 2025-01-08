package com.stotic_dev.FreTreOperation.firebase.collection;

import com.stotic_dev.FreTreOperation.firebase.constant.CollectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SubCollection implements FirestoreCollection {

    private final Logger logger = LoggerFactory.getLogger(SubCollection.class);
    private final List<FirestoreCollection> collectionPath;
    private final CollectionType type;
    private final String documentId;

    public SubCollection(List<FirestoreCollection> collectionPath, CollectionType type, String documentId) {
        this.collectionPath = collectionPath;
        this.type = type;
        this.documentId = documentId;
    }

    @Override
    public String getCollectionId() {
        return type.getCollectionId();
    }

    @Override
    public Optional<SubCollection> getChildCollection(String documentId) {
       if (!hasChild()) { return Optional.empty(); }

       List<FirestoreCollection> collectionPath = this.collectionPath;
       collectionPath.add(this);
       return Optional.of(new SubCollection(collectionPath, type.getChild().get(), documentId));
    }

    @Override
    public boolean hasParent() {
        return true;
    }

    @Override
    public boolean hasChild() {
        return type.getChild().isPresent();
    }

    public FirestoreCollection getParentCollection() {
        return collectionPath.get(collectionPath.size() - 1);
    }

    public List<FirestoreCollection> getCollectionPath() {
        return new ArrayList<>(collectionPath);
    }

    @Override
    public FirestoreCollection getRootCollection() {
        return collectionPath.get(0);
    }

    @Override
    public Optional<String> getDocumentId() {
        return Optional.of(documentId);
    }

    @Override
    public void printDebugDescription() {
        collectionPath.forEach(collection -> logger.info(collection.getDebugDescription()));
        logger.info(getDebugDescription());
    }

    @Override
    public String getDebugDescription() {
        String allowStr = collectionPath.stream().map(e -> "--").collect(Collectors.joining());
        return String.format("documentId = %s:   %s-> %s", getDocumentId().get(), allowStr, getCollectionId());
    }
}
