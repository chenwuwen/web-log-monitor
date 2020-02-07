### WebLogMonitor

#### 1.功能描述
>**WebLogMonitor**是一款轻量的,几乎0侵入式的查看系统日志的插件,适用于java web,传统项目查看日志往往需要登录到服务器
>找到对应的java进程,并查看日志,使用十分不便！使用该插件,可以在浏览器页面查看到实时的日志输出,同时集成了认证功能,大大减轻了调试的难度,目前只支持logback

#### 2.适用场景
> 首先需要声明的是,这个插件并不是用来代替目前主流的日志系统,它存在的意义更多的是方便开发人员进行调试
>同时当你的硬件资源不够强大,或者人员紧缺的情况下,使用该插件可以减轻一定的工作量,从此你再也不为查看日志而手忙脚乱
。因此如果你的系统已经有了比较完善的日志系统,那么这款插件并不适合你,如果你的项目是传统的单体项目,或者你的硬件资源
>不够丰富,但是你又有在线上查看实施日志的需求,那么恭喜你,相信这个日志插件能解决你的需求。


#### 3.如何获得
由于目前是快照版本,因此需要先添加仓库地址
```xml
<repositories>
 
<repository>
    <id>snapshots</id>
    <name>snapshots</name>
    <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
 
</repositories>
```
添加完仓库后,就可以添加依赖了。

```xml
<dependency>
  <groupId>io.github.chenwuwen</groupId>
  <artifactId>web-log-monitor</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>

```



#### 4.如何使用
添加依赖之后,可以通过在classpath 下创建web_log_monitor.properties文件(不是必须),[这里](https://github.com/chenwuwen/web_log_monitor/blob/master/web_log_monitor.properties)
可以在该配置文件中定义自己的配置

配置完成后,在logback的配置文件中的appender中添加一个filter
cn.kanyun.monitor.logback.filter.WebLogFilter

```xml
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <!--格式化输出：%d表示日期，%thread表示线程名，%-5level：级别从左显示5个字符宽度%msg：日志消息，%n是换行符-->
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n</pattern>
        </encoder>
        <filter class="cn.kanyun.monitor.logback.filter.WebLogFilter">
        </filter>
    </appender>
```
这样就配置完成了。


> **注意:** 如果使用的是Springboot,那么还需要在启动类上添加一个@ServletComponentScan("cn.kanyun")注解。


配置完成之后,就可以启动项目了。

启动项目之后。通过输入地址:  项目地址/web/log

就可以打开WebLogMonitor的认证界面,输入用户名密码即可登录

> **默认** 用户名/密码：admin/admin

登录完成就可以查看到实时日志了




