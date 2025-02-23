package com.example.fabricmod.util;

import java.util.*;
import java.util.function.ToIntFunction;

/**
 * 通用权重随机工具类
 * @param <T> 需要随机的元素类型
 */
public class WeightedRandom<T> {
    private final double[] probability;  // 概率数组
    private final int[] alias;          // 别名数组
    private final Random random;
    private final T[] elements;         // 元素数组
    
    /**
     * 使用元素列表和权重函数构造
     * @param elements 元素列表
     * @param weightFunction 获取元素权重的函数
     */
    public WeightedRandom(T[] elements, ToIntFunction<T> weightFunction) {
        this(elements, Arrays.stream(elements)
            .mapToInt(weightFunction)
            .toArray());
    }
    
    /**
     * 使用元素列表和权重数组构造
     * @param elements 元素列表
     * @param weights 对应的权重数组
     */
    public WeightedRandom(T[] elements, int[] weights) {
        if (elements.length != weights.length) {
            throw new IllegalArgumentException("元素数量与权重数量不匹配");
        }
        if (elements.length == 0) {
            throw new IllegalArgumentException("元素列表不能为空");
        }
        
        this.elements = elements.clone();
        this.random = new Random();
        int n = elements.length;
        
        // 初始化数组
        this.probability = new double[n];
        this.alias = new int[n];
        
        // 计算总权重
        double sum = 0;
        for (int weight : weights) {
            if (weight < 0) {
                throw new IllegalArgumentException("权重不能为负数");
            }
            sum += weight;
        }
        if (sum == 0) {
            throw new IllegalArgumentException("总权重不能为0");
        }
        
        // 创建概率和别名工作列表
        double[] prob = new double[n];
        for (int i = 0; i < n; i++) {
            prob[i] = (weights[i] * n) / sum;
        }
        
        // 创建小于1和大于1的概率索引列表
        Deque<Integer> small = new ArrayDeque<>();
        Deque<Integer> large = new ArrayDeque<>();
        
        // 分类概率
        for (int i = 0; i < n; i++) {
            if (prob[i] < 1.0) {
                small.add(i);
            } else {
                large.add(i);
            }
        }
        
        // 处理别名对
        while (!small.isEmpty() && !large.isEmpty()) {
            int less = small.pop();
            int more = large.pop();
            
            probability[less] = prob[less];
            alias[less] = more;
            
            prob[more] = (prob[more] + prob[less]) - 1.0;
            if (prob[more] < 1.0) {
                small.add(more);
            } else {
                large.add(more);
            }
        }
        
        // 处理剩余的项（由于浮点数精度可能存在）
        while (!large.isEmpty()) {
            probability[large.pop()] = 1.0;
        }
        while (!small.isEmpty()) {
            probability[small.pop()] = 1.0;
        }
    }
    
    /**
     * 获取一个随机元素
     * @return 根据权重随机选择的元素
     */
    public T next() {
        int column = random.nextInt(probability.length);
        return elements[random.nextDouble() < probability[column] ? column : alias[column]];
    }
    
    /**
     * 生成一个包含所有元素的随机序列（每个元素出现一次）
     * 权重越大的元素，在序列中越靠前的概率越大
     * @return 随机序列
     */
    public List<T> generateSequence() {
        List<T> allElements = Arrays.asList(elements.clone());
        List<T> sequence = new ArrayList<>(allElements.size());
        
        // 计算每个元素的随机优先级
        Map<T, Double> priorities = new HashMap<>();
        for (T element : allElements) {
            // 使用权重影响的随机数作为优先级
            // 权重越大，生成的随机数范围越大，排在前面的概率就越大
            int index = Arrays.asList(elements).indexOf(element);
            double priority = Math.pow(random.nextDouble(), 1.0 / probability[index]);
            priorities.put(element, priority);
        }
        
        // 根据优先级排序
        allElements.sort((a, b) -> Double.compare(priorities.get(b), priorities.get(a)));
        
        return allElements;
    }
    
    /**
     * 生成多个随机序列
     * @param count 序列数量
     * @return 随机序列列表
     */
    public List<List<T>> generateSequences(int count) {
        List<List<T>> sequences = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            sequences.add(generateSequence());
        }
        return sequences;
    }
    
    /**
     * 设置随机种子
     * @param seed 随机种子
     */
    public void setSeed(long seed) {
        random.setSeed(seed);
    }
} 