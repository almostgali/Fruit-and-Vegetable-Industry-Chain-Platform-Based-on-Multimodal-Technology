package org.back.service.impl;

import org.back.entity.FruitMaturity;
import org.back.entity.FruitQuality;
import org.back.mapper.FruitMaturityMapper;
import org.back.mapper.FruitQualityMapper;
import org.back.service.ModelService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 模型服务实现类
 */
@Service
public class ModelServiceImpl implements ModelService {

    // 模型路径配置
    private static final String QUALITY_MODEL_PATH = "models/fruit_quality_yolo.pt";
    private static final String MATURITY_MODEL_PATH = "models/fruit_freshness_yolo.pt";

    @Value("${model.quality.path:${user.dir}/models/fruit_quality_yolo.pt}")
    private String qualityModelPath;

    @Value("${model.maturity.path:${user.dir}/models/fruit_freshness_yolo.pt}")
    private String maturityModelPath;

    @Value("${upload.path:${user.dir}/uploads}")
    private String uploadPath;

    private final JdbcTemplate jdbcTemplate;
    private final FruitQualityMapper fruitQualityMapper;
    private final FruitMaturityMapper fruitMaturityMapper;

    public ModelServiceImpl(JdbcTemplate jdbcTemplate, FruitQualityMapper fruitQualityMapper, FruitMaturityMapper fruitMaturityMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.fruitQualityMapper = fruitQualityMapper;
        this.fruitMaturityMapper = fruitMaturityMapper;
    }

    @Override
    public Map<String, Object> detectFruitQuality(MultipartFile imageFile) {
        try {
            // 保存上传的图片
            String imagePath = saveImage(imageFile);
            // 转换为可访问的相对 URL（/uploads/filename）
            String imageUrl = "/uploads/" + new java.io.File(imagePath).getName();
            
            // 调用Python脚本执行模型推理
            Map<String, Object> result = executeQualityModel(imagePath);
            // 绑定图片保存路径，便于持久化与前端展示
            if (result != null) {
                result.put("image_url", imageUrl);
            }
            
            // 返回结果
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "检测失败: " + e.getMessage());
            return errorResult;
        }
    }

    @Override
    public Map<String, Object> detectFruitMaturity(MultipartFile imageFile) {
        try {
            // 保存上传的图片
            String imagePath = saveImage(imageFile);
            // 转换为可访问的相对 URL（/uploads/filename）
            String imageUrl = "/uploads/" + new java.io.File(imagePath).getName();
            
            // 调用Python脚本执行模型推理
            Map<String, Object> result = executeMaturityModel(imagePath);
            // 绑定图片保存路径，便于持久化与前端展示
            if (result != null) {
                result.put("image_url", imageUrl);
            }
            
            // 返回结果
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "检测失败: " + e.getMessage());
            return errorResult;
        }
    }

    @Override
    public boolean saveDetectionResult(Long userId, String imageUrl, Map<String, Object> resultMap, String type) {
        try {
            System.out.println("开始保存检测结果 - 用户ID: " + userId + ", 类型: " + type);
            System.out.println("检测结果数据: " + resultMap);
            
            if ("quality".equals(type)) {
                // 保存品质检测结果
                System.out.println("保存品质检测结果...");
                FruitQuality fruitQuality = new FruitQuality();
                 fruitQuality.setUserId(userId);
                 fruitQuality.setImageUrl(imageUrl);
                 fruitQuality.setCategory((String) resultMap.getOrDefault("category", ""));
                 // 将脚本返回的品质等级与评级映射到实体字段
                 fruitQuality.setMaturity((String) resultMap.getOrDefault("quality_level", ""));
                 fruitQuality.setSweetness((String) resultMap.getOrDefault("quality_rating", ""));
                 fruitQuality.setResult((String) resultMap.getOrDefault("result", ""));
                 fruitQuality.setCreateTime(LocalDateTime.now());
                 fruitQuality.setUpdateTime(LocalDateTime.now());
                 
                System.out.println("准备插入品质检测数据: " + fruitQuality);
                fruitQualityMapper.insert(fruitQuality);
                System.out.println("品质检测结果保存成功");
            } else if ("maturity".equals(type)) {
                // 保存成熟度检测结果
                System.out.println("保存成熟度检测结果...");
                FruitMaturity fruitMaturity = new FruitMaturity();
                 fruitMaturity.setUserId(userId);
                 fruitMaturity.setImageUrl(imageUrl);
                 fruitMaturity.setMaturity((String) resultMap.getOrDefault("maturity", ""));
                 fruitMaturity.setResult((String) resultMap.getOrDefault("result", ""));
                 fruitMaturity.setCreateTime(LocalDateTime.now());
                 fruitMaturity.setUpdateTime(LocalDateTime.now());
                 
                System.out.println("准备插入成熟度检测数据: " + fruitMaturity);
                fruitMaturityMapper.insert(fruitMaturity);
                System.out.println("成熟度检测结果保存成功");
            }
            return true;
        } catch (Exception e) {
            System.err.println("保存检测结果时发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 保存上传的图片
     */
    private String saveImage(MultipartFile file) throws IOException {
        // 创建上传目录
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + extension;
        
        // 保存文件
        Path filePath = Paths.get(uploadPath, filename);
        Files.write(filePath, file.getBytes());
        
        return filePath.toString();
    }

    /**
     * 执行水果品质分类模型
     */
    private Map<String, Object> executeQualityModel(String imagePath) throws IOException {
        try {
            // 构建ProcessBuilder调用Python脚本
            // 优先使用项目内 .venv Python，回退到系统 Python
            String userDir = System.getProperty("user.dir");
            String venvPython = Paths.get(userDir, ".venv", "Scripts", "python.exe").toString();
            String pythonPath = new File(venvPython).exists() ? venvPython : "python";
            // 选择脚本路径：优先 target/classes 下的打包脚本，回退到源码目录
            String packagedScript = Paths.get(System.getProperty("user.dir"), "target", "classes", "scripts", "detect.py").toString();
            String sourceScript = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "scripts", "detect.py").toString();
            String scriptPath = new File(packagedScript).exists() ? packagedScript : sourceScript;

            // 处理模型权重路径：若配置路径不存在，尝试使用 back/models 下的默认路径
            String weightsPath = qualityModelPath;
            if (!new File(weightsPath).exists()) {
                String fallback = Paths.get(userDir, "back", "models", "fruit_quality_yolo.pt").toString();
                if (new File(fallback).exists()) {
                    weightsPath = fallback;
                }
            }

            ProcessBuilder pb = new ProcessBuilder(
                pythonPath,
                scriptPath,
                "--source", imagePath,
                "--weights", weightsPath,
                "--type", "quality"
            );
            
            // 设置工作目录
            pb.directory(new File(System.getProperty("user.dir")));
            
            // 合并标准错误和标准输出
            pb.redirectErrorStream(true);
            
            // 启动进程
            Process process = pb.start();
            
            // 读取输出
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            // 等待进程完成
            int exitCode = process.waitFor();
            String outputStr = output.toString().trim();
            
            System.out.println("Python脚本输出: " + outputStr);
            System.out.println("退出码: " + exitCode);
            
            if (exitCode != 0) {
                throw new IOException("Python脚本执行失败，退出码: " + exitCode + ", 输出: " + outputStr);
            }
            
            if (outputStr.isEmpty()) {
                throw new IOException("Python脚本没有输出任何内容");
            }
            
            // 解析JSON输出
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(outputStr, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            // 返回详细错误信息以便调试
            Map<String, Object> result = new HashMap<>();
            result.put("error", "模型调用失败: " + e.getMessage());
            result.put("details", e.toString());
            return result;
        }
    }

    /**
     * 执行水果新鲜度检测模型
     */
    private Map<String, Object> executeMaturityModel(String imagePath) throws IOException {
        try {
            // 构建ProcessBuilder调用Python脚本
            // 优先使用项目内 .venv Python，回退到系统 Python
            String userDir = System.getProperty("user.dir");
            String venvPython = Paths.get(userDir, ".venv", "Scripts", "python.exe").toString();
            String pythonPath = new File(venvPython).exists() ? venvPython : "python";
            // 选择脚本路径：优先 target/classes 下的打包脚本，回退到源码目录
            String packagedScript = Paths.get(System.getProperty("user.dir"), "target", "classes", "scripts", "detect.py").toString();
            String sourceScript = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "scripts", "detect.py").toString();
            String scriptPath = new File(packagedScript).exists() ? packagedScript : sourceScript;

            // 处理模型权重路径：若配置路径不存在，尝试使用 back/models 下的默认路径
            String weightsPath = maturityModelPath;
            if (!new File(weightsPath).exists()) {
                String fallback = Paths.get(userDir, "back", "models", "fruit_freshness_yolo.pt").toString();
                if (new File(fallback).exists()) {
                    weightsPath = fallback;
                }
            }

            ProcessBuilder pb = new ProcessBuilder(
                pythonPath,
                scriptPath,
                "--source", imagePath,
                "--weights", weightsPath,
                "--type", "maturity"
            );
            
            // 设置工作目录
            pb.directory(new File(System.getProperty("user.dir")));
            
            // 合并标准错误和标准输出
            pb.redirectErrorStream(true);
            
            // 启动进程
            Process process = pb.start();
            
            // 读取输出
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            // 等待进程完成
            int exitCode = process.waitFor();
            String outputStr = output.toString().trim();
            
            System.out.println("Python脚本输出: " + outputStr);
            System.out.println("退出码: " + exitCode);
            
            if (exitCode != 0) {
                throw new IOException("Python脚本执行失败，退出码: " + exitCode + ", 输出: " + outputStr);
            }
            
            if (outputStr.isEmpty()) {
                throw new IOException("Python脚本没有输出任何内容");
            }
            
            // 解析JSON输出
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(outputStr, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            // 返回详细错误信息以便调试
            Map<String, Object> result = new HashMap<>();
            result.put("error", "模型调用失败: " + e.getMessage());
            result.put("details", e.toString());
            return result;
        }
    }
}