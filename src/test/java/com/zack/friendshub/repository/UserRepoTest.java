package com.zack.friendshub.repository;

import com.zack.friendshub.BaseIntegrationTest;
import com.zack.friendshub.TestDataFactory;
import com.zack.friendshub.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@Transactional
public class UserRepoTest extends BaseIntegrationTest {
    @Autowired
    UserRepo userRepo;

    @Autowired
    TestDataFactory testDataFactory;

    @Test
    public void shouldReturnUserWhenUsernameExists() {
        User user = testDataFactory.saveUser("testuser");

        Optional<User> foundUser = userRepo.findByUsername("testuser");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getId()).isEqualTo(user.getId());
    }

    @Test
    public void shouldReturnOptionalEmptyWhenUsernameDoesNotExist() {
        User user = testDataFactory.saveUser("testuser");

        Optional<User> foundUser = userRepo.findByUsername("notexist");
        assertThat(foundUser).isNotPresent();
    }

    @Test
    public void shouldBeCaseSensitiveWhenFindingUserByUsername() {
        User user = testDataFactory.saveUser("Testuser");

        Optional<User> foundUser = userRepo.findByUsername("testuser");

        assertThat(foundUser).isEmpty();
    }

    // TODO  existsByUsername, existsByEmail and findByEmail methods to write test cases
}
