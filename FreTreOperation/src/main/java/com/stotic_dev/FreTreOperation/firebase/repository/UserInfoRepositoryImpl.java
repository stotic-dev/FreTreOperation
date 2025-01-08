package com.stotic_dev.FreTreOperation.firebase.repository;

import com.stotic_dev.FreTreOperation.firebase.collection.FirestoreCollection;
import com.stotic_dev.FreTreOperation.firebase.collection.RootCollection;
import com.stotic_dev.FreTreOperation.firebase.constant.CollectionType;
import com.stotic_dev.FreTreOperation.firebase.data.UserInfoData;
import com.stotic_dev.FreTreOperation.firebase.query.AnyQuery;
import com.stotic_dev.FreTreOperation.firebase.query.EqualInQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Repository
public class UserInfoRepositoryImpl {

    Logger logger = LogManager.getLogger(UserInfoRepositoryImpl.class);

    @Autowired
    FirestoreRepositoryImpl firestoreRepository;

    public List<UserInfoData> fetchAll() throws ExecutionException, InterruptedException, TimeoutException {
        return firestoreRepository.fetchDocuments(getUserInfoCollection())
                .stream()
                .map(data -> UserInfoData.buildFromFirestoreData(data.getData()))
                .filter(userInfo -> userInfo.isPresent())
                .map(Optional::get)
                .toList();
    }

//    public List<UserInfoData> fetchUserInfoListByMemberId(List<String> memberIdList) throws ExecutionException, InterruptedException, TimeoutException {
//        EqualInQuery<String> query = new EqualInQuery<>("memberId", memberIdList);
//        return firestoreRepository.fetchDocuments(query.createQuery(CollectionType.USERINFO.getRef())).stream()
//                .map(data -> UserInfoData.buildFromFirestoreData(data.getData()))
//                .filter(userInfo -> userInfo.isPresent())
//                .map(Optional::get)
//                .toList();
//    }

    public List<String> insertUserInfo(List<UserInfoData> dataList) {
        return dataList.stream().map(data -> {
            try {
                return firestoreRepository.insertDocument(getUserInfoCollection(), data.toFirestoreData());
            } catch (Exception e) {
                logger.error("Failed insert user info: %s", data.getMemberId());
                return null;
            }
        })
                .filter(documentId -> documentId != null)
                .toList();
    }

    public void deleteUserInfoListByMemberId(List<String> memberIdList) throws ExecutionException, InterruptedException, TimeoutException {
        EqualInQuery<String> query = new EqualInQuery<>("memberId", memberIdList);
        firestoreRepository.deleteDocumentsWithTransaction(getUserInfoCollection(), query);
    }

    public void deleteAllUserInfo() throws ExecutionException, InterruptedException, TimeoutException {
        firestoreRepository.deleteDocuments(getUserInfoCollection(), new AnyQuery());
    }

    private FirestoreCollection getUserInfoCollection() {
        return new RootCollection(CollectionType.USERINFO);
    }
}
