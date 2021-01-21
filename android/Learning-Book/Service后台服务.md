> 目前为止，本书所有的应用都离不开activity，也就是说它们都有一个或多个看得见的用户界面。
>
> 如果不给应用提供用户界面，应该怎样做呢？如果不用看、不用操作，只想任务在后台运行，
> 如播放音乐或在RSS feed上检查新博文推送，又该如何做呢？好办，使用**服务**(service)。

## 创建IntentService
```java
public class PollService extends IntentService {
    public PollService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}
```
上述代码实现了最基本的`IntentService`。它能做什么呢？实际上，它和`activity`有点像。
它是一个`context`(`Service`是`Context`的子类)，**能够响应intent**(比如`#onHandleIntent`)。

服务的`intent`又叫**命令**(command)。所谓命令，就是要服务做事的一条指令。服务的种类不同，其执行
命令的方式也不尽相同。

 `IntentService`服务能顺序执行命令队列里的命令。

 既然类似于`activity`，能够响应`intent`，就必须在`AndroidManifest.xml`中声明它。
 ```xml
 <application>

    <service android:name=".PollService">

 </application>
 ```

## 启动服务
```java
Intent intent = new Intent(context, PollService.class);
context.startService(intent)
```
 通过定时器setInterval()定时启动

## 服务的能与不能
与activity一样，服务是一个有生命周期回调方法的应用组件。这些回调方法同样也会在主UI线程上运行，和activity里的一样。

**原生服务不能在后台线程上运行。**这也是我们推荐使用`IntentService`的最主要原因。
大多数重要服务都需要在后台线程上运行，而IntentService已提供了一套标准支持方案。

### 服务的生命周期

 - `#onCreate` 服务创建时调用
 - `#onStartCommand(Intent,int,int)`
   每次组件通过`startService(Intent)`方法启动服务时调用一次。它有两个整数参数，一个是标识符集，一个是启动ID。
   标识符集用来表示当前intent发送究竟是一次重新发送，还是一次没成功过的发送。
   每次调用`onStartCommand(Intent,int,int)`方法，启动ID都会不同。因此，启动ID也可用于区分不同的命令。
 - `#onDestroy()` 服务不再需要时调用

服务停止时会调用`onDestroy()`方法。服务停止的方式取决于服务的类型。服务的类型由`onStartCommand(...)`
方法的返回值确定，可能的服务类型有`Service.START_NOT_STICKY`、`START_REDELIVER_INTENT`和`START_STICKY`。


## 合理控制服务启动的频度
后台服务重复性的工作会消耗电池电量和数据流量。并且，启动服务还要唤醒设备，这也是个开销极大的操作。
我们可以通过**确定合理的时间间隔**，**设置唤醒条件**等优化。

### 1.非精准重复
`setRepeating(...)`方法可以设置一个重复定时器，但不是太精准。

