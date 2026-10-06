package meciblock.main.Service;

import meciblock.main.Repository.BlockchainBlockRepository;
import meciblock.main.model.BlockchainBlock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@Service
public class BlockchainService {
    private final BlockchainBlockRepository repository;
    public BlockchainService(BlockchainBlockRepository repository) { this.repository = repository; }

    public String sha256(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new IllegalStateException("SHA-256 unavailable", e); }
    }

    @Transactional
    public BlockchainBlock addBlock(String type, String recordId, String data) {
        BlockchainBlock block = new BlockchainBlock();
        long next = repository.count() + 1;
        String previous = repository.findTopByOrderByBlockIndexDesc().map(BlockchainBlock::getBlockHash).orElse("0".repeat(64));
        String dataHash = sha256(data);
        block.setBlockIndex(next);
        block.setRecordType(type);
        block.setRecordId(recordId);
        block.setDataHash(dataHash);
        block.setPreviousHash(previous);
        block.setTimestamp(LocalDateTime.now());
        block.setBlockHash(sha256(next + "|" + type + "|" + recordId + "|" + dataHash + "|" + previous));
        return repository.save(block);
    }

    public List<BlockchainBlock> all() { return repository.findAllByOrderByBlockIndexDesc(); }

    public boolean verifyChain() {
        List<BlockchainBlock> blocks = new ArrayList<>(repository.findAllByOrderByBlockIndexDesc());
        Collections.reverse(blocks);
        String previous = "0".repeat(64);
        for (BlockchainBlock b : blocks) {
            String expected = sha256(b.getBlockIndex() + "|" + b.getRecordType() + "|" + b.getRecordId() + "|" + b.getDataHash() + "|" + previous);
            if (!expected.equals(b.getBlockHash()) || !previous.equals(b.getPreviousHash())) return false;
            previous = b.getBlockHash();
        }
        return true;
    }
}
