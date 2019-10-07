## Activity生命周期
 用户旋转设备，应用状态就重置，是什么原因呢？
 要想搞明白，并解决这类常见的旋转问题，首先要学习activity生命周期的基础知识。
```java
onCreate()

onStart()

onResume()

onPause()

onStop()

onDestroy
```
#### onCreate

通过覆盖(``@Override`)`onCreate(Bundle)`方法，activity可以预处理以下UI相关工作：
 - 实例化组件并将它们放置在屏幕上(调用`setContentView(int)`方法)；
 - 引用已实例化的组件；
 - 为组件设置监听器已处理用户交互；
 - 访问外部模型数据；

## 设备配置与备选资源
 设备旋转会改变*设备配置(device configuration)*。*设备配置*实际是一系列特征组合，用来
描述设备当前状态。这些特征有：屏幕方向、屏幕像素密度、屏幕尺寸、键盘类型、底座模式以及
语言等。
 通常，为匹配不同的设备配置，应用会提供不同的备选资源。为适应不同分辨率的屏幕，向
项目添加多套箭头图标就是这样一个使用案例。
 设备的屏幕像素密度是个固定的设备配置，无法在运行时发生改变。然而，屏幕方向等特征
可以在应用运行时改变。
 在*运行时配置变更(runtime configuration change)*发生时，可能会有更合适的资源来匹配新
的设备配置。**于是，Android销毁当前activity，为新配置寻找最佳资源，然后创建新实例使用这**
**些资源。**。

#### 创建水平模式布局
Android依靠res子目录的配置修饰符定位最佳资源以匹配当前设备配置。
设备处于水平方向时，Android会找到并使用`res/layout-land`目录下的布局资源。
其他情况下，它会默认使用`res/layout`目录下的布局资源。

## 保存数据以应对设备旋转
覆盖以下Activity方法就是一种解决方案：
```java
 protected void onSaveInstanceState(Bundle outState)
```
该方法通常在`onStop()`方法之前由系统调用，除非用户按后退键。
方法**onSaveInStanceState(Bundle)**的默认实现要求所有activity视图将自身状态数据保存在
Bundle对象中。**Bundle是存储字符串键与限定类型值之间映射关系（键-值对）的一种结构**,
具体操作为覆盖**onSaveInStanceState(Bundle)**方法：

```java
private int mCurrentIndex;

@Override
public void onSaveInstanceState(Bundle saveInstanceState) {
    super.onSaveInstanceState(saveInstanceState);

    saveInstanceState.putInt(KEY_INDEX, mCurrentIndex);

}

//在onCreate(Bundle)方法中检查存储的bundle信息
protected void onCreate(Bundle saveInstanceState) {
    super.onCreate(saveInstanceState);

    if(saveInstanceState != null) {
        mCurrentIndex = saveInstanceState.getInt(KEY_INDEX, default:0);
    }
}
```
 注意，在Bundle中存储和恢复的数据类型只能是*基本类型(promitive type)*以及可以实现
`Serializable`或`Parcelable`接口的对象。
 在Bundle中保存定制**类对象**不是个好主意，因为你取回的对象可能已经没用了。比较好的做法是，
通过其他方式保存定制类对象，而在Bundle中保存标识对象的基本类型数据。

## 再探activity生命周期
`saveInstanceState`是如何保存数据的？*暂存状态(stashed state)*;
activity进入暂存状态并不一定需要调用onDestroy()方法。常见的做法是：
 - 覆盖saveInstanceState方法，在Bundle对象中，保存当前activity的小的或暂存状态的数据
 - 覆盖`onStop()`方法，保存永久性数据，如用户编辑的文字等。（onStop()方法调用完，
   activity才会被系统摧毁，所以用它保存永久性数据）