package pt.ulisboa.tecnico.withoutnet.db;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import io.reactivex.rxjava3.core.Single;
import pt.ulisboa.tecnico.withoutnet.models.Message;
import pt.ulisboa.tecnico.withoutnet.models.MessageType;
import pt.ulisboa.tecnico.withoutnet.models.MessageType.*;

@Dao
public interface MessageDao {
    @Query("SELECT * FROM message")
    Single<List<Message>> getAll();

    @Query("SELECT * FROM message WHERE messageType = (:messageType)")
    Single<List<Message>> findByMessageType(MessageType messageType);

    @Query("SELECT * FROM message WHERE sender = (:senderId)")
    Single<List<Message>> findBySender(int senderId);

    @Query("SELECT * FROM message WHERE receiver = (:receiverId)")
    Single<List<Message>> findByReceiver(int receiverId);

    @Query("SELECT * FROM message WHERE sender = (:senderId) AND receiver = (:receiverId)")
    Single<List<Message>> findBySenderAndReceiver(int senderId, int receiverId);
}
