package com.zack.friendshub.repository;

import com.zack.friendshub.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendshipRepo extends JpaRepository<Friendship, Long> {

    @Query("""
                SELECT COUNT(f) > 0 FROM Friendship f
                WHERE (f.requester.id = :u1 AND f.addressee.id = :u2)
                   OR (f.requester.id = :u2 AND f.addressee.id = :u1)
            """)
    boolean existsBetweenUsers(Long u1, Long u2);

    @Query("""
    SELECT f FROM Friendship f
    WHERE f.status = 'ACCEPTED'
    AND (f.requester.id = :userId OR f.addressee.id = :userId)
""")
    List<Friendship> findAllFriends(@Param("userId") Long userId);


}
