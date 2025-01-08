package com.stotic_dev.FreTreOperation.firebase.collection;

import com.stotic_dev.FreTreOperation.firebase.constant.CollectionType;

import java.util.List;
import java.util.Optional;

public interface FirestoreCollection {
    public String getCollectionId();
    public Optional<SubCollection> getChildCollection(String documentId);
    public boolean hasParent();
    public boolean hasChild();
    public List<FirestoreCollection> getCollectionPath();
    public FirestoreCollection getRootCollection();
    public Optional<String> getDocumentId();
    public void printDebugDescription();
    public String getDebugDescription();
}
