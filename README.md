<h1>Android进程保活·双进程拉活(Java层)</h1>
<h2>Android进程</h2>
<br><br>
首先你要知道Android中的进程以及它的优先级，下面来说明它进程<br>
<ol>
	<li><font color="red">前台进程 (Foreground process)</font></li>
	<li><font color="red">可见进程 (Visible process)<font color="red"></li>
	<li><font color="red">服务进程 (Service process)<font color="red"></li>
	<li><font color="red">后台进程 (Background process)<font color="red"></li>
	<li><font color="red">空进程 (Empty process)<font color="red"></li>
</ol>

下面进行解释：<br><br><br>
<h3><font color="red">前台进程(Foreground process):</font></h3>
用户当前操作所必需的进程。如果一个进程满足以下任一条件，即视为前台进程：

<ul>
	<li>托管用户正在交互的 Activity（已调用 Activity 的 onResume() 方法）</li>
	<li>托管某个 Service，后者绑定到用户正在交互的 Activity</li>
	<li>托管正在“前台”运行的 Service（服务已调用 startForeground()）</li>
	<li>托管正执行一个生命周期回调的 Service（onCreate()、onStart() 或 onDestroy()）</li>
	<li>托管正执行其 onReceive() 方法的 BroadcastReceiver</li>
</ul>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;通常，在任意给定时间前台进程都为数不多。只有在内存不足以支持它们同时继续运行这一万不得已的情况下，系统才会终止它们。 此时，设备往往已达到内存分页状态，因此需要终止一些前台进程来确保用户界面正常响应。<br><br><br>

<h3><font color="red">可见进程 (Visible process):</font></h3>
没有任何前台组件、但仍会影响用户在屏幕上所见内容的进程。 如果一个进程满足以下任一条件，即视为可见进程：
<ul>
	<li>托管不在前台、但仍对用户可见的 Activity（已调用其 onPause() 方法）。例如，如果前台 Activity 启动了一个对话框，允许在其后显示上一 Activity，则有可能会发生这种情况。</li>
	<li>托管绑定到可见（或前台）Activity 的 Service。</li>
</ul>

可见进程被视为是极其重要的进程，除非为了维持所有前台进程同时运行而必须终止，否则系统不会终止这些进程。<br><br><br>

<h3><font color="red">服务进程 (Service process):</font></h3>
正在运行已使用 startService() 方法启动的服务且不属于上述两个更高类别进程的进程。尽管服务进程与用户所见内容没有直接关联，但是它们通常在执行一些用户关心的操作（例如，在后台播放音乐或从网络下载数据）。因此，除非内存不足以维持所有前台进程和可见进程同时运行，否则系统会让服务进程保持运行状态。<br><br><br>

<h3><font color="red">后台进程 (Service process):</font></h3>
包含目前对用户不可见的 Activity 的进程（已调用 Activity 的 onStop() 方法）。这些进程对用户体验没有直接影响，系统可能随时终止它们，以回收内存供前台进程、可见进程或服务进程使用。 通常会有很多后台进程在运行，因此它们会保存在 LRU （最近最少使用）列表中，以确保包含用户最近查看的 Activity 的进程最后一个被终止。如果某个 Activity 正确实现了生命周期方法，并保存了其当前状态，则终止其进程不会对用户体验产生明显影响，因为当用户导航回该 Activity 时，Activity 会恢复其所有可见状态。<br><br><br>

<h3><font color="red">空进程 (Empty process):</font></h3>
不含任何活动应用组件的进程。保留这种进程的的唯一目的是用作缓存，以缩短下次在其中运行组件所需的启动时间。 为使总体系统资源在进程缓存和底层内核缓存之间保持平衡，系统往往会终止这些进程。<br><br><br>

<h3><font color="red">进程优先级:</font></h3>
首先空进程是最先被回收的，其次便是后台进程，依次往上，前台进程是最后才会被结束。<br><br><br>

<h2>Android进程保活</h2>
有很多种方法可以实现Android的进程保活，比如通过&nbsp;<font color="blue">1像素且透明Activity提升App进程优先级</font>、<font color="blue">通过设置前台Service提升App进程优先级</font>、<font color="blue">Java层的双进程拉活</font>、<font color="skyblue">JobScheduler实现</font>、<font color="skyblue">NDK双进程守护</font>、<font color="skyblue">使用账户同步拉活</font>、<font color="skyblue">workmanager实现</font>。<br><br>
下面这幅图，说明的是：
<ul>
	<li>红色部分是容易被回收的进程，属于android进程</li>
	<li>绿色部分是较难被回收的进程，属于android进程</li>
	<li>其他部分则不是android进程，也不会被系统回收，一般是ROM自带的app和服务才能拥有</li>
</ul>

![在asdf这里插入图片描述](https://img-blog.csdnimg.cn/2018122010382369.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQwODgxNjgw,size_16,color_FFFFFF,t_70)

本篇文章介绍的是进程第三种方式:
<ul>
	<li><font color="red">双进程拉活(Java层)</font></li>
</ul>

<h5><font color="red">双进程拉活(Java层):</font></h5>
<br>当一个进程结束后，立刻调用启动另一个进程，这样实现互相调用，互相启动(<font color="red">只有在一个进程结束时候才会启动另一个进程</font>)


<br>首先创建LocalService.java继承自Service(android.app.Service):↓
```java
public class LocalService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*第一个参数Intent
        第二个参数ServiceConnection*/

        /**
         *  第三个参数介绍：
        * Flag for {@link #bindService}: automatically create the service as long
        * as the binding exists.  Note that while this will create the service,
        * its {@link android.app.Service#onStartCommand}
        * method will still only be called due to an
        * explicit call to {@link #startService}.  Even without that, though,
        * this still provides you with access to the service object while the
        * service is created.
        *
        * <p>Note that prior to {@link android.os.Build.VERSION_CODES#ICE_CREAM_SANDWICH},
        * not supplying this flag would also impact how important the system
        * consider's the target service's process to be.  When set, the only way
        * for it to be raised was by binding from a service in which case it will
        * only be important when that activity is in the foreground.  Now to
        * achieve this behavior you must explicitly supply the new flag
        * {@link #BIND_ADJUST_WITH_ACTIVITY}.  For compatibility, old applications
        * that don't specify {@link #BIND_AUTO_CREATE} will automatically have
        * the flags {@link #BIND_WAIVE_PRIORITY} and
        * {@link #BIND_ADJUST_WITH_ACTIVITY} set for them in order to achieve
        * the same result.
        */
        bindService(new Intent(this,RemoteService.class),connection,Context.BIND_AUTO_CREATE);
        return super.onStartCommand(intent, flags, startId);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //绑定成功
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //当RemoteService所处进程被干掉就重新启动
            startService(new Intent(LocalService.this,RemoteService.class));
            bindService(new Intent(LocalService.this,RemoteService.class),connection,Context.BIND_IMPORTANT);
        }
    };

    private class LocalBinder extends Binder {

    }
}
```
<br>
对LocalService在清单文件中进行注册

```html
<service android:name=".LocalService" />
```


<br>
创建RemoteService.java继承自Service(android.app.Service):↓

```java
public class RemoteService extends Service {
    public RemoteService() {
    
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new RemoteBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /*第一个参数Intent
        第二个参数ServiceConnection
        第三个参数介绍：

        /**
        * Flag for {@link #bindService}: automatically create the service as long
        * as the binding exists.  Note that while this will create the service,
        * its {@link android.app.Service#onStartCommand}
        * method will still only be called due to an
        * explicit call to {@link #startService}.  Even without that, though,
        * this still provides you with access to the service object while the
        * service is created.
        *
        * <p>Note that prior to {@link android.os.Build.VERSION_CODES#ICE_CREAM_SANDWICH},
        * not supplying this flag would also impact how important the system
        * consider's the target service's process to be.  When set, the only way
        * for it to be raised was by binding from a service in which case it will
        * only be important when that activity is in the foreground.  Now to
        * achieve this behavior you must explicitly supply the new flag
        * {@link #BIND_ADJUST_WITH_ACTIVITY}.  For compatibility, old applications
        * that don't specify {@link #BIND_AUTO_CREATE} will automatically have
        * the flags {@link #BIND_WAIVE_PRIORITY} and
        * {@link #BIND_ADJUST_WITH_ACTIVITY} set for them in order to achieve
        * the same result.
        */
        bindService(new Intent(this,RemoteService.class),connection,Context.BIND_AUTO_CREATE);
        return super.onStartCommand(intent, flags, startId);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //绑定成功
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //当RemoteService所处进程被干掉就重新启动
            startService(new Intent(RemoteService.this,LocalService.class));
            bindService(new Intent(RemoteService.this,LocalService.class),connection,Context.BIND_IMPORTANT);
        }
    };

    private class RemoteBinder extends Binder{
    
    }
}

```
<br>
对RemoteService 在清单文件中进行注册，再制定一个进程名字，好区分

```html
<service
            android:name=".RemoteService"
            android:enabled="true"
            android:exported="true"
            android:process=":remote" />
        <!--
            android:process=":remote"
            指定进程名
        -->
```

<br>最后在MainActivity启动其中一个服务LocalService:

```java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //双进程拉活(Java层)
        startService(new Intent(this,LocalService.class));
    }
}
```

这样就完成了双进程拉活
