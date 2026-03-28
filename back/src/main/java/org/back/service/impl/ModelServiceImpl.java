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

    private String resolvePythonExecutable(String userDir) {
        String osName = String.valueOf(System.getProperty("os.name", "")).toLowerCase();
        boolean isWindows = osName.contains("win");

        if (isWindows) {
            String venvPython = Paths.get(userDir, ".venv", "Scripts", "python.exe").toString();
            if (new File(venvPython).exists()) {
                return venvPython;
            }
            return "python";
        }

        String venvPython = Paths.get(userDir, ".venv", "bin", "python").toString();
        if (new File(venvPython).exists()) {
            return venvPython;
        }
        return "python3";
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
            // 保存上传的图片或视频
            String imagePath = saveImage(imageFile);
            // 转换为可访问的相对 URL（/uploads/filename）
            String imageUrl = "/uploads/" + new java.io.File(imagePath).getName();
            
            // 调用Python脚本执行模型推理
            Map<String, Object> result = executeMaturityModel(imagePath);
            
            // 绑定保存路径
            if (result != null) {
                result.put("image_url", imageUrl);
                
                // 【多模态融合】模拟IoT传感器数据并进行预测
                Map<String, Object> iotData = simulateIoTData();
                result.put("iot_data", iotData);

                Map<String, Object> vision = new HashMap<>();
                vision.put("maturity", result.getOrDefault("maturity", "未知"));
                vision.put("maturity_level", result.getOrDefault("maturity_level", "unknown"));
                vision.put("category", result.getOrDefault("category", "水果"));
                vision.put("confidence", result.getOrDefault("confidence", 0.0));
                vision.put("confidence_desc", result.getOrDefault("confidence_desc", ""));
                vision.put("raw_class", result.getOrDefault("raw_class", ""));
                vision.put("detection_type", result.getOrDefault("detection_type", ""));

                Map<String, Object> fusionResult = executeFusionModel(vision, iotData);
                Object fusionPrediction = fusionResult.get("fusion_prediction");
                if (fusionPrediction == null) {
                    fusionPrediction = generateMultimodalPredictionFallback(
                            (String) vision.getOrDefault("maturity", "未知"),
                            (String) vision.getOrDefault("category", "水果"),
                            iotData
                    );
                }

                result.put("fusion_prediction", String.valueOf(fusionPrediction));
                result.put("fusion", fusionResult);
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

    /**
     * 模拟冷链车厢IoT传感器数据
     */
    private Map<String, Object> simulateIoTData() {
        Map<String, Object> iot = new HashMap<>();
        // 模拟过去24小时数据
        iot.put("temperature_avg", String.format("%.1f", 4.5 + Math.random() * 2)); // 4.5~6.5 ℃
        iot.put("temperature_fluctuation", String.format("%.1f", 1.0 + Math.random() * 3)); // 波动 1.0~4.0 ℃
        iot.put("humidity", String.format("%.1f", 85.0 + Math.random() * 10)); // 湿度 85%~95%
        iot.put("ethylene", String.format("%.2f", 0.5 + Math.random() * 1.5)); // 乙烯浓度 ppm
        return iot;
    }

    /**
     * 结合视觉检测结果与IoT时序数据进行多模态预测
     */
    private String generateMultimodalPrediction(String maturity, String category, Map<String, Object> iot) {
        try {
            Map<String, Object> vision = new HashMap<>();
            vision.put("maturity", maturity);
            vision.put("category", category);
            Map<String, Object> fusionResult = executeFusionModel(vision, iot);
            Object prediction = fusionResult.get("fusion_prediction");
            if (prediction != null) {
                return String.valueOf(prediction);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return generateMultimodalPredictionFallback(maturity, category, iot);
    }

    private String generateMultimodalPredictionFallback(String maturity, String category, Map<String, Object> iot) {
        double tempAvg = Double.parseDouble((String) iot.get("temperature_avg"));
        double tempFluc = Double.parseDouble((String) iot.get("temperature_fluctuation"));
        double ethylene = Double.parseDouble((String) iot.get("ethylene"));

        StringBuilder prediction = new StringBuilder();
        prediction.append("【多模态融合分析】\n");
        prediction.append("综合当前视觉状态（").append(maturity).append("）");
        prediction.append("与过去24小时冷链IoT数据（平均温度").append(tempAvg).append("℃，波动").append(tempFluc).append("℃）：\n");

        if ("腐烂".equals(maturity)) {
            prediction.append("该批次已变质，且乙烯浓度偏高（").append(ethylene).append("ppm），建议立即隔离，避免加速同车厢其他果蔬腐烂。");
        } else {
            double shelfLife = 7.0;
            if (tempAvg > 5.0) shelfLife -= (tempAvg - 5.0) * 0.5;
            if (tempFluc > 2.0) shelfLife -= 1.0;
            if (ethylene > 1.0) shelfLife -= (ethylene - 1.0) * 1.5;

            if (shelfLife < 0) shelfLife = 0.5;

            prediction.append("预计该批次「").append(category).append("」剩余保质期还有 ").append(String.format("%.1f", shelfLife)).append(" 天。");

            if (tempFluc > 2.0) {
                prediction.append(" ⚠️警告：近期温度波动较大，建议检查冷库设备。");
            }
        }
        return prediction.toString();
    }

    private Map<String, Object> executeFusionModel(Map<String, Object> vision, Map<String, Object> iot) throws IOException {
        try {
            String userDir = System.getProperty("user.dir");
            String pythonPath = resolvePythonExecutable(userDir);

            String packagedScript = Paths.get(userDir, "target", "classes", "scripts", "fusion_predict.py").toString();
            String sourceScript = Paths.get(userDir, "src", "main", "resources", "scripts", "fusion_predict.py").toString();
            String scriptPath = new File(packagedScript).exists() ? packagedScript : sourceScript;

            ProcessBuilder pb = new ProcessBuilder(pythonPath, scriptPath);
            pb.directory(new File(userDir));
            pb.redirectErrorStream(true);
            pb.environment().put("PYTHONIOENCODING", "utf-8");
            pb.environment().put("PYTHONUTF8", "1");

            Process process = pb.start();

            Map<String, Object> input = new HashMap<>();
            input.put("vision", vision);
            input.put("iot", iot);

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String inputJson = mapper.writeValueAsString(input);

            try (java.io.BufferedWriter writer = new java.io.BufferedWriter(
                    new java.io.OutputStreamWriter(process.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                writer.write(inputJson);
            }

            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            String outputStr = output.toString().trim();

            if (exitCode != 0) {
                throw new IOException("融合脚本执行失败，退出码: " + exitCode + ", 输出: " + outputStr);
            }
            if (outputStr.isEmpty()) {
                throw new IOException("融合脚本没有输出任何内容");
            }

            int jsonStart = outputStr.indexOf("{");
            int jsonEnd = outputStr.lastIndexOf("}");
            if (jsonStart != -1 && jsonEnd != -1) {
                outputStr = outputStr.substring(jsonStart, jsonEnd + 1);
            }

            return mapper.readValue(outputStr, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("error", "融合模型调用失败: " + e.getMessage());
            result.put("details", e.toString());
            return result;
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
            String pythonPath = resolvePythonExecutable(userDir);
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
            pb.environment().put("PYTHONIOENCODING", "utf-8");
            pb.environment().put("PYTHONUTF8", "1");
            
            // 启动进程
            Process process = pb.start();
            
            // 读取输出
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
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
            
            // 提取JSON部分，忽略可能的警告或错误日志
            int jsonStart = outputStr.indexOf("{");
            int jsonEnd = outputStr.lastIndexOf("}");
            if (jsonStart != -1 && jsonEnd != -1) {
                outputStr = outputStr.substring(jsonStart, jsonEnd + 1);
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
            String pythonPath = resolvePythonExecutable(userDir);
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
            pb.environment().put("PYTHONIOENCODING", "utf-8");
            pb.environment().put("PYTHONUTF8", "1");
            
            // 启动进程
            Process process = pb.start();
            
            // 读取输出
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
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
            
            // 提取JSON部分，忽略可能的警告或错误日志
            int jsonStart = outputStr.indexOf("{");
            int jsonEnd = outputStr.lastIndexOf("}");
            if (jsonStart != -1 && jsonEnd != -1) {
                outputStr = outputStr.substring(jsonStart, jsonEnd + 1);
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
