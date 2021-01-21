## 启动activity
 一个activity启动另一个activity最简单的方式是使用`startActivity`方法
 ```java
 public void startActivity(Intent intent);
 ```

## 基于intent的通信
 intent对象是component用来与操作系统通信的一种媒介工具。目前为止，我们唯一见过的
component就是`activity`。实际上还有其他一些component：`service`、`broadcast receiver`以及
`content provider`。
 intent用来告诉ActivityManager该启动哪个activity
 ```java
 public Intent(Context packageContext, Class<?> cls)
 ```
比如（下方为显式intent）：
```java
Intent intent = new Intent(QuizActivity.this, CheatActivity.class);
startActivity(intent);
```

## activity间的数据传递
#### 使用intent extra
```java
public Intent putExtra(String name, boolean value);
```
也可以使用静态方法封装传递数据的逻辑
```java
public static Intent newIntent(Context packageContext, boolean answerIsTrue) {
    Intent intent = new Intent(packageContext, CheatActivity.class);
    intent.putExtra(EXTRA_ANSWER_IS_TRUE, answerIsTrue);
    return intent;
}

//使用in QuizActivity.class
Intent intent = CheatActivity.newIntent(QuizActivity.this, true);
startActivity(intent);
```

#### 从子activity获取返回结果
需要从子activity获取返回信息时，可调用以下Activity方法：
```java
public void startActivityForResult(Intent intent, int requestCode);
```
#### 主动设置返回结果
```java
public final void setResult(int resultCode)
public final void setResult(int resultCode, Intent data)

/**
 * resultCode常量也可以使用系统内置的
 * Activity.RESULT_OK
 * Activity.RESULT_CANCELED

```

## activity回退栈
```java
activity.finish()
```