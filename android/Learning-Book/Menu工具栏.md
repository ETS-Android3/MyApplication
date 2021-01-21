# 在XML文件中定义菜单

右键单击`res`目录，选择`New -> Android resource file`菜单项。`Resource Type`类型选择`Menu`资源类型。

```xml
    <menu xmlns:android="http://schemas.android.com/apk/res/android"
            xmlns:app="http://schemas.android.com/apk/res-auto">
        <item
            android:id="@+id/new_crime"
            android:icon="@android:drawable/ic_menu_add"
            android:title="@string/new_crime"
            app:showAsAction="ifRoom|withText"/>
    </menu>
```

 `showAsAction`属性用于指定菜单项是显示在工具栏上，还是隐藏于***溢出***菜单(overflow menu)。
该属性当前设置为`ifRoom`和`withText`的组合值。因此，只要空间足够，菜单项图标就会显示在工具栏上，
空间不足时就会隐藏在*溢出*菜单中。

## 系统自带的Icon Type

右键单击`drawable`目录，选择`New -> Image Asset`菜单项。在`Icon Type`一栏选`Action Bar and Tab Icons`

# 创建菜单

 - Activity 提供了管理菜单的回调函数`onCreateOptionsMenu(Menu)`
 - Fragment 也有一套自己的选项菜单回调函数
   ```java
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
   ```

当activity接收到操作系统的`onCreateOptionsMenu(...)`方法回调请求时，我们
必须明确告诉`FragmentManager`：其管理的fragment应接收`onCreateOptionsMenu(...)`方法的
调用指令。要通知`FragmentManager`，需调用以下方法：
```java
    public void setHasOptionsMenu(boolean hasMenu);
```