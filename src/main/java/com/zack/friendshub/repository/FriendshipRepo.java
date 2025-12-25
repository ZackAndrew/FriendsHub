package com.zack.friendshub.repository;

import com.zack.friendshub.model.Friendship;
import com.zack.friendshub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendshipRepo extends JpaRepository<Friendship, Long> {
    boolean existsByAddressee(User addressee);

    @Query("""
                SELECT COUNT(f) > 0 FROM Friendship f
                WHERE (f.requester.id = :u1 AND f.addressee.id = :u2)
                   OR (f.requester.id = :u2 AND f.addressee.id = :u1)
            """)
    boolean existsBetweenUsers(Long u1, Long u2);
}
