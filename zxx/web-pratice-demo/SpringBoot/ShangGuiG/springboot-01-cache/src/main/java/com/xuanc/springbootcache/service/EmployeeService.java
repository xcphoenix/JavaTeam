package com.xuanc.springbootcache.service;

import com.xuanc.springbootcache.bean.Employee;
import com.xuanc.springbootcache.mapper.EmployeeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author xuanc
 * @version 1.0
 * @date 2019/8/6 上午8:17
 */
@Service
public class EmployeeService {

    @Resource
    EmployeeMapper employeeMapper;

    /**
     * CacheManger 管理多个 Cache 组件，对缓存的真正 crud 操作在 Cache 组件中，每一个缓存组件都有自己唯一的名字
     * <p>
     * - cacheNames/value: 指定缓存组件的名字；数组的方式可以指定多个缓存
     * - key: 指定缓存数据时使用的 key（默认使用方法参数的值　参数值－方法的返回值）[可以使用 SpEL]
     * - keyGenerator：　key 的生成器，可以指定 key 的生成器组件 id
     * key 与 keyGenerator: 二选一
     * - cacheManager：指定缓存管理器（或者指定 cacheResolver）
     * - condition: 指定符合条件的情况下，才缓存
     * - unless；当指定的条件为 true，就不会被缓存，可以获取到结果进行判断
     * - sync:：是否使用异步模式，默认为 false，启用时 unless 不生效
     * <p>
     * 原理：
     * 　1. 自动配置类：CacheAutoConfiguration
     * 2. 缓存的配置类
     * 5 = "org.springframework.boot.autoconfigure.cache.CouchbaseCacheConfiguration"
     * 7 = "org.springframework.boot.autoconfigure.cache.CaffeineCacheConfiguration"
     * 6 = "org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration"
     * 0 = "org.springframework.boot.autoconfigure.cache.GenericCacheConfiguration"
     * 3 = "org.springframework.boot.autoconfigure.cache.HazelcastCacheConfiguration"
     * 4 = "org.springframework.boot.autoconfigure.cache.InfinispanCacheConfiguration"
     * 2 = "org.springframework.boot.autoconfigure.cache.EhCacheCacheConfiguration"
     * 1 = "org.springframework.boot.autoconfigure.cache.JCacheCacheConfiguration"
     * 8 = "org.springframework.boot.autoconfigure.cache.SimpleCacheConfiguration"
     * 9 = "org.springframework.boot.autoconfigure.cache.NoOpCacheConfiguration"
     * 3. 哪些配置类会默认生效：SimpleCacheConfiguration
     * 4. 给容器中注册了一个 CacheManager：ConcurrentMapCacheManager
     * 5. 可以获取和创建 ConcurrentMapCache 类型的缓存组件，作用是将数据保存在 ConcurrentMap 中
     * <p>
     * 运行步骤：
     *
     */
    @Cacheable(cacheNames = "{emp}", key = "#id", condition = "#id>0")
    public Employee getEmp(Integer id) {
        System.out.println("查询" + id + "号员工");
        return employeeMapper.getEmpById(id);
    }

    @CachePut(/*value = "emp",*/key = "#result.id")
    public Employee updateEmp(Employee employee) {
        System.out.println("updateEmp:" + employee);
        employeeMapper.updateEmp(employee);
        return employee;
    }

    /**
     * @CacheEvict：缓存清除 key：指定要清除的数据
     * allEntries = true：指定清除这个缓存中所有的数据
     * beforeInvocation = false：缓存的清除是否在方法之前执行
     * 默认代表缓存清除操作是在方法执行之后执行;如果出现异常缓存就不会清除
     * <p>
     * beforeInvocation = true：
     * 代表清除缓存操作是在方法运行之前执行，无论方法是否出现异常，缓存都清除
     */
    @CacheEvict(value = "emp", beforeInvocation = true/*key = "#id",*/)
    public void deleteEmp(Integer id) {
        System.out.println("deleteEmp:" + id);
        //employeeMapper.deleteEmpById(id);
        int i = 10 / 0;
    }

    // @Caching 定义复杂的缓存规则
    @Caching(
            cacheable = {
                    @Cacheable(value="emp",key = "#lastName")
            },
            put = {
                    @CachePut(value="emp",key = "#result.id"),
                    @CachePut(value="emp",key = "#result.email")
            }
    )
    public Employee getEmpByLastName(String lastName) {
        return employeeMapper.getEmpByLastName(lastName);
    }

}
