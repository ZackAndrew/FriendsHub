package com.zack.friendshub.repository;

import com.zack.friendshub.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendshipRepo extends JpaRepository<Friendship, Long> {

    @Query("""
                SELECT COUNT(f) > 0 FROM Friendship f
                WHERE (f.requester.id = :u1 AND f.addressee.id = :u2)
                   OR (f.requester.id = :u2 AND f.addressee.id = :u1)
            """)
    boolean existsBetweenUsers(Long u1, Long u2);

    Friendship getFriendshipsById(Long id);
}
