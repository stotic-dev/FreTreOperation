package com.stotic_dev.FreTreOperation.firebase.repository;

import com.stotic_dev.FreTreOperation.firebase.collection.FirestoreCollection;
import com.stotic_dev.FreTreOperation.firebase.collection.RootCollection;
import com.stotic_dev.FreTreOperation.firebase.collection.SubCollection;
import com.stotic_dev.FreTreOperation.firebase.constant.CollectionType;
import com.stotic_dev.FreTreOperation.firebase.data.FirestoreDocumentData;
import com.stotic_dev.FreTreOperation.firebase.data.TokenData;
import com.stotic_dev.FreTreOperation.firebase.data.TokenMemberData;
import com.stotic_dev.FreTreOperation.firebase.query.AnyQuery;
import com.stotic_dev.FreTreOperation.firebase.query.EqualFieldQuery;
import com.stotic_dev.FreTreOperation.firebase.query.FirestoreQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Repository
public class TokenMemberInfoRepositoryImpl {

    Logger logger = LogManager.getLogger(TokenMemberInfoRepositoryImpl.class);
    @Autowired FirestoreRepositoryImpl firestoreRepository;

    public List<TokenData> fetchALLTokenMembersPerTeam() throws ExecutionException, InterruptedException, TimeoutException {
        List<FirestoreDocumentData> allTokenTeamData = fetchAllTokenTeamData();
        return allTokenTeamData
                .stream()
                .map(data -> {
                    try {
                        List<TokenMemberData> members = fetchTeamMembersData(data.documentId);
                        return new TokenData(data.getData(), data.documentId, members);
                    }
                    catch (Exception e) {
                        logger.warn(String.format("Failed fetch team member data(documentId=%s, reason=%s)", data.documentId, e.getMessage()));
                        return null;
                    }
                })
                .filter(result -> result != null)
                .toList();
    }

    public String insertTokenData(TokenData data) throws ExecutionException, InterruptedException {
        String insertDocumentId = firestoreRepository.insertDocument(getTokenTeamCollection(), data.getFirestoreData(), data.teamUUID);

        for (TokenMemberData member : data.getMembers()) {
            firestoreRepository.insertDocument(getTeamMemberCollection(insertDocumentId), member.getFirestoreData());
        }
        return insertDocumentId;
    }

    public void deleteTokenTeam(String teamUUID) {
        firestoreRepository.deleteDocumentWithTransaction(getTokenTeamCollection(), teamUUID);
    }

    public void deleteTokenTeamMember(String teamUUID, String memberId) throws ExecutionException, InterruptedException, TimeoutException {
        FirestoreQuery deleteMemberQuery = new EqualFieldQuery("memberId", memberId);
        firestoreRepository.deleteDocumentsWithTransaction(getTeamMemberCollection(teamUUID), deleteMemberQuery);
    }

    public void deleteAllTokenData() throws ExecutionException, InterruptedException, TimeoutException {
        List<FirestoreDocumentData> allTokenData = fetchAllTokenTeamData();
        for (FirestoreDocumentData tokenData : allTokenData) {
            firestoreRepository.deleteDocuments(getTeamMemberCollection(tokenData.documentId), new AnyQuery());
        }
        firestoreRepository.deleteDocuments(getTokenTeamCollection(), new AnyQuery());
    }

    private FirestoreCollection getTokenTeamCollection() {
        return new RootCollection(CollectionType.TOKEN_TEAM);
    }

    private FirestoreCollection getTeamMemberCollection(String documentId) {
        return new SubCollection(List.of(getTokenTeamCollection()), CollectionType.TOKEN_MEMBER, documentId);
    }

    private List<FirestoreDocumentData> fetchAllTokenTeamData() throws ExecutionException, InterruptedException, TimeoutException {
        return firestoreRepository.fetchDocuments(getTokenTeamCollection());
    }

    private List<TokenMemberData> fetchTeamMembersData(String documentId) throws ExecutionException, InterruptedException, TimeoutException {
        return firestoreRepository.fetchDocuments(getTeamMemberCollection(documentId))
                .stream()
                .map(data -> new TokenMemberData(data.getData()))
                .toList();
    }
}
