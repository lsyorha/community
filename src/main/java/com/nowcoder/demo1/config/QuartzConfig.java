package com.nowcoder.demo1.config;

import com.nowcoder.demo1.quartz.PostScoreRefreshJob;
import com.nowcoder.demo1.quartz.YorhaJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

// 配置 -> 数据库 -> 调用
@Configuration
public class QuartzConfig {
//    FactoryBean用于简化Bean的实例化过程
//    1、通过factoryBean封装bean的实例化过程
//    2、将factoryBean装配到Spring容器里
//    3、将factoryBean注入给其他的bean
//    4、该bean得到的就是factoryBean所管理的对象实例

//    用于简化配置JobDetail
//    @Bean
    public JobDetailFactoryBean yorhaJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(YorhaJob.class);
        factoryBean.setBeanName("yorhaJob");
//        多个任务可以同处一组
        factoryBean.setGroup("yorhaGroup");
//      任务保存是否持久化
        factoryBean.setDurability(true);
//      任务是否可以恢复
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

//    配置Trigger（SimpleTriggerFactoryBean，CronTriggerFactoryB）
//    @Bean
    public SimpleTriggerFactoryBean yorhaTrigger(JobDetail yorhaJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
//        优先注入同名的bean
        factoryBean.setJobDetail(yorhaJobDetail);
        factoryBean.setBeanName("yorhaTrigger");
        factoryBean.setGroup("yorhaTriggerGroup");
//      执行频率
        factoryBean.setRepeatInterval(3000);
//      存job的状态
        factoryBean.setJobDataMap(new JobDataMap());
        return  factoryBean;
    }

//    刷新帖子分数任务
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setBeanName("postScoreRefreshJob");
//        多个任务可以同处一组
        factoryBean.setGroup("communityJobGroup");
//      任务保存是否持久化
        factoryBean.setDurability(true);
//      任务是否可以恢复
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
//        优先注入同名的bean
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        factoryBean.setBeanName("postScoreRefreshTrigger");
        factoryBean.setGroup("communityTriggerGroup");
//      执行频率
        factoryBean.setRepeatInterval(1000 * 60 * 5);
//      存job的状态
        factoryBean.setJobDataMap(new JobDataMap());
        return  factoryBean;
    }
}
