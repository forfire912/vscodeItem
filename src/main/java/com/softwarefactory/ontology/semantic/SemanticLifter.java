package com.softwarefactory.ontology.semantic;

import com.softwarefactory.ontology.model.Link;
import com.softwarefactory.ontology.model.OntologyObject;
import com.softwarefactory.ontology.engine.StateMonitor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 语义提升器 - 将异构工具的原始数据转换为本体对象
 */
public class SemanticLifter {
    private StateMonitor stateMonitor;
    private Map<String, DataMapper> mappers;

    public SemanticLifter(StateMonitor stateMonitor) {
        this.stateMonitor = stateMonitor;
        this.mappers = new HashMap<>();
        
        // 注册默认映射器
        registerDefaultMappers();
    }

    /**
     * 注册数据映射器
     */
    public void registerMapper(String sourceType, DataMapper mapper) {
        mappers.put(sourceType, mapper);
    }

    /**
     * 摄入原始数据并转换为本体对象
     */
    public void ingestData(String sourceType, Map<String, Object> rawData) {
        System.out.println("\n=== 语义提升: " + sourceType + " ===");
        
        DataMapper mapper = mappers.get(sourceType);
        if (mapper != null) {
            List<OntologyObject> objects = mapper.map(rawData);
            
            // 注册到状态监视器
            for (OntologyObject obj : objects) {
                stateMonitor.registerObject(obj);
            }
            
            System.out.println("提升了 " + objects.size() + " 个本体对象");
        } else {
            System.out.println("警告: 未找到 " + sourceType + " 的映射器");
        }
    }

    /**
     * 注册默认映射器
     */
    private void registerDefaultMappers() {
        // Git提交映射器
        registerMapper("git_commit", new GitCommitMapper());
        
        // 需求映射器
        registerMapper("requirement", new RequirementMapper());
        
        // 缺陷映射器
        registerMapper("defect", new DefectMapper());
    }

    /**
     * 数据映射器接口
     */
    public interface DataMapper {
        List<OntologyObject> map(Map<String, Object> rawData);
    }

    /**
     * Git提交映射器
     */
    public static class GitCommitMapper implements DataMapper {
        private static final Pattern FIXES_PATTERN = Pattern.compile("Fixes\\s+#(\\d+)");
        private static final Pattern IMPLEMENTS_PATTERN = Pattern.compile("Implements\\s+#(\\d+)");

        @Override
        public List<OntologyObject> map(Map<String, Object> rawData) {
            List<OntologyObject> objects = new ArrayList<>();
            
            String commitId = (String) rawData.get("commit_id");
            String message = (String) rawData.get("message");
            String author = (String) rawData.get("author");
            List<String> files = (List<String>) rawData.getOrDefault("files", new ArrayList<>());
            
            // 创建代码变更对象
            OntologyObject changeObj = new OntologyObject(
                "change_" + commitId,
                "code_change",
                "代码变更 " + commitId.substring(0, Math.min(8, commitId.length()))
            );
            
            changeObj.setAttribute("commit_id", commitId);
            changeObj.setAttribute("message", message);
            changeObj.setAttribute("author", author);
            changeObj.setAttribute("files", files);
            changeObj.setAttribute("file_count", files.size());
            
            changeObj.addAction("代码走查");
            changeObj.addAction("静态分析");
            
            objects.add(changeObj);
            
            // 解析提交信息中的关联
            parseCommitMessage(message, changeObj);
            
            System.out.println("  解析Git提交: " + commitId);
            
            return objects;
        }

        private void parseCommitMessage(String message, OntologyObject changeObj) {
            // 查找 "Fixes #1024" 模式
            Matcher fixesMatcher = FIXES_PATTERN.matcher(message);
            if (fixesMatcher.find()) {
                String defectId = fixesMatcher.group(1);
                Link link = new Link(
                    UUID.randomUUID().toString(),
                    "fixes",
                    changeObj.getId(),
                    "defect_" + defectId
                );
                changeObj.addLink(link);
                changeObj.setAttribute("fixes_defect", defectId);
                System.out.println("    -> 发现缺陷修复关联: #" + defectId);
            }
            
            // 查找 "Implements #2048" 模式
            Matcher implMatcher = IMPLEMENTS_PATTERN.matcher(message);
            if (implMatcher.find()) {
                String reqId = implMatcher.group(1);
                Link link = new Link(
                    UUID.randomUUID().toString(),
                    "implements",
                    changeObj.getId(),
                    "req_" + reqId
                );
                changeObj.addLink(link);
                changeObj.setAttribute("implements_requirement", reqId);
                System.out.println("    -> 发现需求实现关联: #" + reqId);
            }
        }
    }

    /**
     * 需求映射器
     */
    public static class RequirementMapper implements DataMapper {
        @Override
        public List<OntologyObject> map(Map<String, Object> rawData) {
            List<OntologyObject> objects = new ArrayList<>();
            
            String reqId = (String) rawData.get("id");
            String title = (String) rawData.get("title");
            String securityLevel = (String) rawData.getOrDefault("security_level", "B");
            String status = (String) rawData.getOrDefault("status", "open");
            
            OntologyObject reqObj = new OntologyObject(
                "req_" + reqId,
                "requirement",
                title
            );
            
            reqObj.setAttribute("requirement_id", reqId);
            reqObj.setAttribute("security_level", securityLevel);
            reqObj.setAttribute("status", status);
            
            objects.add(reqObj);
            
            System.out.println("  解析需求: " + reqId + " [安全等级: " + securityLevel + "]");
            
            return objects;
        }
    }

    /**
     * 缺陷映射器
     */
    public static class DefectMapper implements DataMapper {
        @Override
        public List<OntologyObject> map(Map<String, Object> rawData) {
            List<OntologyObject> objects = new ArrayList<>();
            
            String defectId = (String) rawData.get("id");
            String title = (String) rawData.get("title");
            String severity = (String) rawData.getOrDefault("severity", "medium");
            String status = (String) rawData.getOrDefault("status", "open");
            
            OntologyObject defectObj = new OntologyObject(
                "defect_" + defectId,
                "defect",
                title
            );
            
            defectObj.setAttribute("defect_id", defectId);
            defectObj.setAttribute("severity", severity);
            defectObj.setAttribute("status", status);
            
            objects.add(defectObj);
            
            System.out.println("  解析缺陷: " + defectId + " [严重程度: " + severity + "]");
            
            return objects;
        }
    }
}
