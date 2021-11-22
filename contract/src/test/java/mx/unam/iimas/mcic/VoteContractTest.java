package mx.unam.iimas.mcic;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public final class VoteContractTest {

    @Nested
    class AssetExists {
        @Test
        public void noProperAsset() {
            VoteContract contract = new VoteContract();
            Context context = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(context.getStub()).thenReturn(stub);

            String key = "1001";
            when(stub.getState(key)).thenReturn(new byte[] {});
            boolean result = contract.voteExists(context, key);

            assertFalse(result);
        }

        @Test
        public void assetExists() {
            VoteContract contract = new VoteContract();
            Context context = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(context.getStub()).thenReturn(stub);

            String key = "1001";
            when(stub.getState(key)).thenReturn(new byte[] {42});
            boolean result = contract.voteExists(context, key);

            assertTrue(result);
        }

        @Test
        public void noKey() {
            VoteContract contract = new VoteContract();
            Context context = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(context.getStub()).thenReturn(stub);

            String key = "1002";
            when(stub.getState(key)).thenReturn(null);
            boolean result = contract.voteExists(context, key);

            assertFalse(result);
        }
    }

    @Nested
    class AssetCreates {
        @Test
        public void newAssetCreate() {
            VoteContract contract = new VoteContract();
            Context context = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(context.getStub()).thenReturn(stub);

            String json = "{\"value\":\"TheVote\"}";
            String key = "1001";
            contract.createVote(context, key, "TheVote");

            verify(stub).putState(key, json.getBytes(UTF_8));
        }

        @Test
        public void alreadyExists() {
            VoteContract contract = new  VoteContract();
            Context context = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(context.getStub()).thenReturn(stub);

            when(stub.getState("1002")).thenReturn(new byte[] { 42 });

            Exception thrown = assertThrows(RuntimeException.class,
                    () -> contract.createVote(context, "1002", "TheVote"));

            assertEquals(thrown.getMessage(), "The asset 1002 already exists");
        }
    }

    @Test
    public void assetRead() {
        VoteContract contract = new  VoteContract();
        Context context = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);
        when(context.getStub()).thenReturn(stub);

        Vote asset = new  Vote();
        asset.setValue("Valuable");

        String json = asset.toJSONString();
        when(stub.getState("1001")).thenReturn(json.getBytes(StandardCharsets.UTF_8));

        Vote returnedAsset = contract.readVote(context, "1001");
        assertEquals(returnedAsset.getValue(), asset.getValue());
    }

    @Nested
    class AssetUpdates {
        @Test
        public void updateExisting() {
            VoteContract contract = new  VoteContract();
            Context context = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(context.getStub()).thenReturn(stub);
            when(stub.getState("1001")).thenReturn(new byte[] { 42 });

            contract.updateVote(context, "1001", "updates");

            String json = "{\"value\":\"updates\"}";
            verify(stub).putState("1001", json.getBytes(UTF_8));
        }

        @Test
        public void updateMissing() {
            VoteContract contract = new  VoteContract();
            Context context = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(context.getStub()).thenReturn(stub);

            when(stub.getState("1001")).thenReturn(null);

            Exception thrown = assertThrows(RuntimeException.class,
                    () -> contract.updateVote(context, "1001", "TheVote"));

            assertEquals(thrown.getMessage(), "The asset 1001 does not exist");
        }
    }

    @Test
    public void assetDelete() {
        VoteContract contract = new  VoteContract();
        Context context = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);
        when(context.getStub()).thenReturn(stub);
        when(stub.getState("1001")).thenReturn(null);

        Exception thrown = assertThrows(RuntimeException.class,
                () -> contract.deleteVote(context, "1001"));

        assertEquals(thrown.getMessage(), "The asset 1001 does not exist");

    }
}