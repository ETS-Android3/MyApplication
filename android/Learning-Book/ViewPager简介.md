# ViewPager 与 PagerAdapter

 ViewPager在某种程度上类似于RecyclerView。RecyclerView需借助于Adapter提供视图。
同样，ViewPager需要PagerAdapter的支持。
不过相较于RecyclerView与Adapter间的协同工作，ViewPager与PagerAdapter间的配合
要复杂得多。好在Google提供了PagerAdapter的子类`FragmentStatePagerAdapter`，
化繁为简，提供了两个有用的方法:`getCount()`和`getItem(int)`。调用`getItem(int)`方法
获取并显示crime数组中指定位置的Crime时，它会返回配置过的`CrimeFragment`来显示指定的Crime。