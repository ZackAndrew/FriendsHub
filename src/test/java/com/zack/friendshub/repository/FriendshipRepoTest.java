package com.zack.friendshub.repository;

import com.zack.friendshub.BaseIntegrationTest;
import com.zack.friendshub.TestDataFactory;
import com.zack.friendshub.enums.FriendshipStatus;
import com.zack.friendshub.model.Friendship;
import com.zack.friendshub.model.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@Transactional
public class FriendshipRepoTest extends BaseIntegrationTest {

    @Autowired
    FriendshipRepo friendshipRepo;

    @Autowired
    TestDataFactory testDataFactory;

    @Test
    public void shouldFindFriendIdsRegardlessOfWhoIsRequester() {
        User user1 = testDataFactory.saveUser("user1");
        User user2 = testDataFactory.saveUser("user2");
        User user3 = testDataFactory.saveUser("user3");

        testDataFactory.saveFriendship(user1, user2, FriendshipStatus.ACCEPTED);
        testDataFactory.saveFriendship(user3, user1, FriendshipStatus.ACCEPTED);

        List<Long> friendsIds = friendshipRepo.findAllFriendIdsByUserId(user1.getId());

        assertThat(friendsIds).hasSize(2);
        assertThat(friendsIds).containsExactlyInAnyOrder(user2.getId(), user3.getId());
    }

    @Test
    public void shouldReturnEmptyListWhenUserHasNoFriendsAtAll() {
        User lonelyUser = testDataFactory.saveUser("lonelyUser");

        List<Long> friendsIds = friendshipRepo.findAllFriendIdsByUserId(lonelyUser.getId());

        assertThat(friendsIds).isEmpty();
    }

    @Test
    public void shouldCheckIfFriendshipExistsBetweenTwoUsers() {
        User u1 = testDataFactory.saveUser("u1");
        User u2 = testDataFactory.saveUser("u2");
        testDataFactory.saveFriendship(u1, u2, FriendshipStatus.PENDING);

        boolean existsDirect = friendshipRepo.existsBetweenUsers(u1.getId(), u2.getId());
        boolean existsReverse = friendshipRepo.existsBetweenUsers(u2.getId(), u1.getId());

        assertThat(existsDirect).isTrue();
        assertThat(existsReverse).isTrue();
    }

    @Test
    public void shouldFindFriendshipBetweenTwoUsers() {
        User u1 = testDataFactory.saveUser("u1");
        User u2 = testDataFactory.saveUser("u2");
        testDataFactory.saveFriendship(u1, u2, FriendshipStatus.ACCEPTED);

        Optional<Friendship> found = friendshipRepo.findFriendshipBetween(u2.getId(), u1.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getRequester().getId()).isEqualTo(u1.getId());
    }

    @Test
    public void shouldFindAllPendingRequestsForAddressee() {
        User sender1 = testDataFactory.saveUser("sender1");
        User sender2 = testDataFactory.saveUser("sender2");
        User me = testDataFactory.saveUser("me");

        testDataFactory.saveFriendship(sender1, me, FriendshipStatus.PENDING);
        testDataFactory.saveFriendship(sender2, me, FriendshipStatus.ACCEPTED);

        List<Friendship> pending = friendshipRepo.findAllByAddresseeIdAndStatus(me.getId(), FriendshipStatus.PENDING);

        assertThat(pending).hasSize(1);
        assertThat(pending.get(0).getRequester().getUsername()).isEqualTo("sender1");
    }
}
