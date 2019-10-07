## Android应用属于典型的事件驱动类型

 不像命令行或脚本程序，事件驱动应用启动后，
即开始等待行为事件的发生，如用户点击某个按钮。（事件也可以由操作系统或其他应用触发，
但用户触发的事件更直观，如点击按钮。）
 应用等待某个特定事件的发生，也可以说应用正在"监听"特定事件。为响应某个事件而创建
的对象叫做*监听器(listener)*。*监听器*会实现特定事件的*监听器接口(listener interface)*。
```java
//OnClickListener is a listener interface,也属于匿名内部类
mTrueButton = (Button) findViewById(R.id.true_button);
mTrueButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v){
        //Does nothing yet, but soon!
    }
})
```