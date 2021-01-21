# RecyclerView、ViewHolder 和 Adapter

## RecyclerView

如何展示100个View在一个列表中？按需创建视图对象才是比较合理的解决方案。
RecyclerView就是这么做的。它只创建刚好充满屏幕的12个View，而不是100个。
用户滑动屏幕切换视图时，上一个视图会回收利用。

## ViewHolder和Adapter

RecyclerView的任务仅限于回收和定位屏幕上的View。列表项View能够显示数据还离不开
另外两个类的支持：ViewHolder子类和Adapter子类。

ViewHolder只做一件事：**容纳View视图**

```java
//创建一个ViewHolder
public class ListRow extends RecyclerView.ViewHolder {
    public ImageView mThumbnail;

    public ListRow(View view){
        super(view);

        mThumbnail = (ImageView) view.findViewById(R.id.thumbnail);
    }
}

//如何使用：
ListRow row = new ListRow(inflater.inflate(R.layout.list_row, parent, false));
View view = row.itemView;
ImageView thumbnailView = row.mThumbnail;
```

## Adapter
RecyclerView自己不创建ViewHolder。这个任务实际是由Adapter来完成的。
Adapter是一个控制器对象，从模型层获取数据，然后提供给RecyclerView显示，是沟通的桥梁。