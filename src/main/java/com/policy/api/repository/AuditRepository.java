package com.policy.api.repository;

import com.policy.api.model.Audit;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AuditRepository {
    private final Map<String, Audit> map;

    public AuditRepository(){
        this.map = new HashMap<>();
    }

    public Audit save(Audit audit){
        map.put(audit.getAuditId(), audit);
        return audit;
    }

    public List<Audit> get() {
        return new ArrayList<>(map.values());
    }
}
