package ru.practicum.shareit.request;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    @Query("select r " +
            "from ItemRequest r " +
            //"left join Item i on r.id = i.requestId " +
            "where r.requestor.id = ?1 " +
            "ORDER BY r.id ASC")
    List<ItemRequest> getAllByRequestorId(long requestorId);

    @Query("select r " +
            "from ItemRequest r " +
            "where r.requestor.id != ?1 " +
            "ORDER BY r.created DESC")
    List<ItemRequest> findAllItems(long requestorId, Pageable pageable);


    Optional<ItemRequest> findById(Long id);

}
