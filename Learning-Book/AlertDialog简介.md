# 创建DialogFragment

 建议将AlertDialog封装在DialogFragment（Fragment的子类）实例中使用。当然，
不使用DialogFragment也可以显示AlertDialog视图，但不推荐这样做。使用FragmentManager
管理对话框，可以更灵活地显示对话框。
 **另外，如果旋转设备，单独使用的AlertDialog会消失，而封装在Fragment中的AlertDialog**
**则不会有次问题（旋转后，对话框会被重建恢复）。**