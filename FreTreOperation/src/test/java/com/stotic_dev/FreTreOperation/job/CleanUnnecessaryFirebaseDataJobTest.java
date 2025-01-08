package com.stotic_dev.FreTreOperation.job;

import com.stotic_dev.FreTreOperation.config.TestConfig;
import com.stotic_dev.FreTreOperation.constant.OperationMode;
import com.stotic_dev.FreTreOperation.firebase.collection.FirestoreCollection;
import com.stotic_dev.FreTreOperation.firebase.collection.RootCollection;
import com.stotic_dev.FreTreOperation.firebase.collection.SubCollection;
import com.stotic_dev.FreTreOperation.firebase.constant.CollectionType;
import com.stotic_dev.FreTreOperation.firebase.data.*;
import com.stotic_dev.FreTreOperation.firebase.query.EqualFieldQuery;
import com.stotic_dev.FreTreOperation.firebase.query.EqualInQuery;
import com.stotic_dev.FreTreOperation.firebase.query.FirestoreQuery;
import com.stotic_dev.FreTreOperation.firebase.repository.FirestoreRepositoryImpl;
import com.stotic_dev.FreTreOperation.firebase.transaction.FirestoreTransactionClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {TestConfig.class}
)
class CleanUnnecessaryFirebaseDataJobTest {

    @Autowired
    private CleanUnnecessaryFirebaseDataJob targetJob;

    @MockBean
    private FirestoreTransactionClient firestoreTransactionClient;
    @MockBean
    private FirestoreRepositoryImpl firestoreRepository;

    // Test cases method

    @Test
    void emptyCleanTargetCase() throws Exception {
        // スタブの設定
        List<UserInfoData> allUserInfoList = getTestUserInfoDataList(Instant.now(), Instant.ofEpochSecond(Instant.now().getEpochSecond() - 31536000));
        setupFirestoreMock(allUserInfoList, List.of());

        // テスト対象メソッド実行
        targetJob.execute(OperationMode.TEST);

        // 削除処理が行われていないことを確認
        verify(firestoreRepository, never()).deleteDocuments(any(), any());
        verify(firestoreRepository, never()).deleteDocument(any(), any());
    }

    @Test
    void hasCleanTargetCase() throws Exception {
        // スタブの設定
        List<UserInfoData> allUserInfoList = getTestUserInfoDataList(Instant.now(), Instant.ofEpochSecond(Instant.now().getEpochSecond() - 31536001), Instant.ofEpochSecond(0));
        List<TokenData> allTokenDataList = getTestTokenDataList(allUserInfoList.stream().limit(2).toList(), allUserInfoList.stream().skip(2).toList());
        setupFirestoreMock(allUserInfoList, allTokenDataList);

        // テスト対象メソッド実行
        targetJob.execute(OperationMode.TEST);

        // 検証
        assertionResult(
                allUserInfoList.stream().skip(1).map(UserInfoData::getMemberId).toList(),
                List.of(
                        new TokenData(allTokenDataList.get(0).teamId, allTokenDataList.get(0).teamUUID, allTokenDataList.get(0).getMembers().stream().skip(1).toList()),
                        new TokenData(allTokenDataList.get(1).teamId, allTokenDataList.get(1).teamUUID, allTokenDataList.get(1).getMembers())
                ),
                List.of(allTokenDataList.get(1).teamUUID)
        );
    }

    @Test
    void allCleanCase() throws Exception {
        // スタブの設定
        List<UserInfoData> allUserInfoList = getTestUserInfoDataList(Instant.ofEpochSecond(0));
        List<TokenData> allTokenDataList = getTestTokenDataList(allUserInfoList);
        setupFirestoreMock(allUserInfoList, allTokenDataList);

        // テスト対象メソッド実行
        targetJob.execute(OperationMode.TEST);

        // 検証
        assertionResult(
                allUserInfoList.stream().map(UserInfoData::getMemberId).toList(),
                allTokenDataList,
                allTokenDataList.stream().map(team -> team.teamUUID).toList()
        );
    }

    @Test
    void notExistsDeleteTargetInChatMessage() throws Exception {
        // スタブの設定
        List<UserInfoData> allUserInfoList = getTestUserInfoDataList(Instant.now(), Instant.ofEpochSecond(Instant.now().getEpochSecond() - 31536001));
        List<TokenData> allTokenDataList = getTestTokenDataList(allUserInfoList);
        setupFirestoreMock(allUserInfoList, allTokenDataList);

        // テスト対象メソッド実行
        targetJob.execute(OperationMode.TEST);

        // 検証
        assertionResult(
                allUserInfoList.stream().skip(1).map(UserInfoData::getMemberId).toList(),
                List.of(
                        new TokenData(allTokenDataList.get(0).teamId, allTokenDataList.get(0).teamUUID, allTokenDataList.get(0).getMembers().stream().skip(1).toList())
                ),
                List.of()
        );
    }

    // Test utility method

    private void setupFirestoreMock(List<UserInfoData> allUserInfoList, List<TokenData> allTokenDataList) throws Exception {
        // 各情報のドキュメント取得のスタブ設定
        when(firestoreRepository.fetchDocuments(getCollectionMockArg(new RootCollection(CollectionType.USERINFO))))
                .thenReturn(allUserInfoList.stream().map(user -> new FirestoreDocumentData(UUID.randomUUID().toString(), user.toFirestoreData())).toList());
        when(firestoreRepository.fetchDocuments(getCollectionMockArg(new RootCollection(CollectionType.TOKEN_TEAM))))
                .thenReturn(allTokenDataList.stream().map(team -> new FirestoreDocumentData(team.teamUUID, team.getFirestoreData())).toList());
        for (TokenData tokenData : allTokenDataList) {
            when(firestoreRepository.fetchDocuments(getCollectionMockArg(
                    new SubCollection(List.of(new RootCollection(CollectionType.TOKEN_TEAM)), CollectionType.TOKEN_MEMBER, tokenData.teamUUID))
            ))
                    .thenReturn(
                            tokenData.getMembers()
                                    .stream()
                                    .map(member -> new FirestoreDocumentData(UUID.randomUUID().toString(), member.getFirestoreData()))
                                    .toList()
                    );
        }

        List<ChatMessageData> allChatMessageDataList = getTestChatMessageDataList(allTokenDataList.stream().map(tokenData -> tokenData.teamUUID).toList());
        when(firestoreRepository.fetchDocuments(getCollectionMockArg(new RootCollection(CollectionType.CHAT))))
                .thenReturn(allChatMessageDataList.stream().map(chatMessageData -> new FirestoreDocumentData(UUID.randomUUID().toString(), chatMessageData.getFirestoreData())).toList());

        // トランザクション処理のスタブ設定
        doNothing().when(firestoreTransactionClient)
                        .startTransaction();
        doNothing().when(firestoreTransactionClient)
                .commitTransaction();

        // 削除処理のスタブ設定
        doNothing().when(firestoreRepository)
                .deleteDocumentsWithTransaction(any(), any());
        doNothing().when(firestoreRepository)
                .deleteDocumentWithTransaction(any(), any());
    }

    private List<UserInfoData> getTestUserInfoDataList(Instant... updateDateTimeValues) {
        return Arrays.stream(updateDateTimeValues)
                .map(
                        dateTime -> new UserInfoData(UUID.randomUUID().toString(),
                                "",
                                "",
                                64.2,
                                173,
                                false,
                                "",
                                56.1,
                                65,
                                1,
                                false,
                                dateTime)
                )
                .toList();
    }

    private List<TokenData> getTestTokenDataList(List<UserInfoData>... userInfoData) {
        return Arrays.stream(userInfoData)
                .map(userInfoList -> new TokenData(
                        String.format("team%s", userInfoList.get(0).getMemberId()),
                        UUID.randomUUID().toString(),
                        userInfoList.stream().map(user -> new TokenMemberData(user.getMemberId(), UUID.randomUUID().toString())).toList()
                ))
                .toList();
    }

    private List<ChatMessageData> getTestChatMessageDataList(List<String> teamUUIDList) {
        return teamUUIDList
                .stream()
                .map(teamUUID -> new ChatMessageData("userName",
                        "message",
                        "",
                        teamUUID,
                        Instant.now(),
                        "memberId"))
                .toList();
    }

    private FirestoreCollection getCollectionMockArg(FirestoreCollection targetCollection) {
        ArgumentMatcher<FirestoreCollection> isArg = arg -> compareCollection(arg, targetCollection);
        return argThat(isArg);
    }

    private void assertionResult(List<String> deleteTargetUserIdList,
                                 List<TokenData> deleteTargetTeamMemberCollectionList,
                                 List<String> deleteTargetTeamUUIDList) throws ExecutionException, InterruptedException, TimeoutException {
        List<FirestoreCollection> expectedDeleteDocumentsCollectionArgs = new ArrayList<>();
        List<FirestoreQuery> expectedDeleteDocumentsQueryArgs = new ArrayList<>();
        List<FirestoreCollection> expectedDeleteDocumentCollectionArgs = new ArrayList<>();
        List<String> expectedDeleteDocumentIdArgs = new ArrayList<>();

        if (!deleteTargetUserIdList.isEmpty()) {
            // ユーザー情報削除時の期待値を追加
            expectedDeleteDocumentsCollectionArgs.add(new RootCollection(CollectionType.USERINFO));
            expectedDeleteDocumentsQueryArgs.add(new EqualInQuery<>("memberId", deleteTargetUserIdList));
        }

        if (!deleteTargetTeamMemberCollectionList.isEmpty()) {
            // チームメンバー削除時の期待値を追加
            deleteTargetTeamMemberCollectionList.forEach(team -> {
                expectedDeleteDocumentsCollectionArgs.add(new SubCollection(List.of(new RootCollection(CollectionType.TOKEN_TEAM)), CollectionType.TOKEN_MEMBER, team.teamUUID));

                team.getMembers().forEach(member -> expectedDeleteDocumentsQueryArgs.add(new EqualFieldQuery<>("memberId", member.memberId)));
            });
        }

        if (!deleteTargetTeamUUIDList.isEmpty()) {
            deleteTargetTeamUUIDList.forEach(teamUUID -> {
                // チーム情報削除時の期待値を追加
                expectedDeleteDocumentCollectionArgs.add(new RootCollection(CollectionType.TOKEN_TEAM));
                expectedDeleteDocumentIdArgs.add(teamUUID);

                // チャット情報削除時の期待値を追加
                expectedDeleteDocumentsCollectionArgs.add(new RootCollection(CollectionType.CHAT));
                expectedDeleteDocumentsQueryArgs.add(new EqualFieldQuery<>("teamUUID", teamUUID));
            });
        }

        // 削除処理のメソッド呼び出し数と、引数を検証する
        ArgumentCaptor<FirestoreCollection> deleteDocumentsCollectionArgCaptor = ArgumentCaptor.forClass(FirestoreCollection.class);
        ArgumentCaptor<FirestoreQuery> queryArgCaptor = ArgumentCaptor.forClass(FirestoreQuery.class);
        verify(firestoreRepository, times(expectedDeleteDocumentsCollectionArgs.size()))
                .deleteDocumentsWithTransaction(deleteDocumentsCollectionArgCaptor.capture(), queryArgCaptor.capture());

        assertCollectionArgs(
                deleteDocumentsCollectionArgCaptor.getAllValues(),
                expectedDeleteDocumentsCollectionArgs
        );
        assertQueryArgs(
                queryArgCaptor.getAllValues(),
                expectedDeleteDocumentsQueryArgs
        );

        ArgumentCaptor<FirestoreCollection> deleteDocumentCollectionArgCaptor = ArgumentCaptor.forClass(FirestoreCollection.class);
        ArgumentCaptor<String> deleteDocumentIdArgCaptor = ArgumentCaptor.forClass(String.class);
        verify(firestoreRepository, times(expectedDeleteDocumentCollectionArgs.size()))
                .deleteDocumentWithTransaction(
                        deleteDocumentCollectionArgCaptor.capture(),
                        deleteDocumentIdArgCaptor.capture()
                );

        assertCollectionArgs(
                deleteDocumentCollectionArgCaptor.getAllValues(),
                expectedDeleteDocumentCollectionArgs
        );
        Assertions.assertEquals(deleteDocumentIdArgCaptor.getAllValues(), expectedDeleteDocumentIdArgs);
    }

    private boolean compareCollection(FirestoreCollection lhs, FirestoreCollection rhs) {
        if (lhs == null) { return false; }
        if (!lhs.getCollectionId().equals(rhs.getCollectionId())) { return false; }
        if (lhs.getDocumentId().isPresent() && !lhs.getDocumentId().get().equals(rhs.getDocumentId().get())) {
            return false;
        }

        List<FirestoreCollection> lhsCollectionPath = lhs.getCollectionPath();
        List<FirestoreCollection> rhsCollectionPath = rhs.getCollectionPath();
        if (lhsCollectionPath.size() != rhsCollectionPath.size()) { return false; }

        if (lhsCollectionPath.isEmpty()) { return true; }

        boolean isEqualPath = false;
        for (FirestoreCollection lhsParent : lhsCollectionPath) {
            int targetIndex = lhsCollectionPath.indexOf(lhsParent);
            FirestoreCollection rhsParent = rhs.getCollectionPath().get(targetIndex);
            isEqualPath = compareCollection(lhsParent, rhsParent);
            if (!isEqualPath) { break; }
        }

        return isEqualPath;
    }

    private void assertCollectionArgs(List<FirestoreCollection> actual, List<FirestoreCollection> expected) {
        Assertions.assertEquals(actual.size(), expected.size());
        for (FirestoreCollection actualCollection : actual) {
            int index = actual.indexOf(actualCollection);
            Assertions.assertTrue(compareCollection(actualCollection, expected.get(index)));
        }
    }

    private void assertQueryArgs(List<FirestoreQuery> actual, List<FirestoreQuery> expected) {
        Assertions.assertEquals(actual.size(), expected.size());
        for (FirestoreQuery actualQuery : actual) {
            int index = actual.indexOf(actualQuery);
            FirestoreQuery expectedQuery = expected.get(index);
            Assertions.assertInstanceOf(expectedQuery.getClass(), actualQuery);
            Assertions.assertEquals(actualQuery, expectedQuery);
        }
    }
}