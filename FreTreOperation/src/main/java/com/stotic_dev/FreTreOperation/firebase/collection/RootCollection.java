package com.stotic_dev.FreTreOperation.firebase.collection;

import com.stotic_dev.FreTreOperation.firebase.constant.CollectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public final class RootCollection implements FirestoreCollection {
    private final CollectionType type;

    private Logger logger = LoggerFactory.getLogger(RootCollection.class);

    public RootCollection(CollectionType type) {
        this.type = type;
    }

    @Override
    public String getCollectionId() {
        return type.getCollectionId();
    }

    @Override
    public Optional<SubCollection> getChildCollection(String documentId) {
        if (!hasChild()) { return Optional.empty(); }

        return Optional.of(new SubCollection(List.of(this), type.getChild().get(), documentId));
    }

    @Override
    public boolean hasParent() {
        return false;
    }

    @Override
    public boolean hasChild() {
        return type.getChild().isPresent();
    }

    @Override
    public List<FirestoreCollection> getCollectionPath() {
        return List.of();
    }

    @Override
    public FirestoreCollection getRootCollection() {
        return this;
    }

    @Override
    public Optional<String> getDocumentId() {
        return Optional.empty();
    }

    @Override
    public void printDebugDescription() {
        logger.info(getDebugDescription());
    }

    @Override
    public String getDebugDescription() {
        return String.format("root -> %s", getCollectionId());
    }
}
