## 引入fragment

> 采用fragment而不是activity来管理应用UI，可绕开Android系统activity使用规则的限制。
> 整个生命周期过程中，activity视图还是那个视图。因此不必担心会违反Androi系统的activity使用规则。

#### 两类fragment
 fragment是在API 11级系统版本中引入的。现在，Google有两个版本的fragment实现可供选择：
原生版本和支持库版本(AppCompat库 `compile 'com.android.support:appcompat-v7:25.0.1'`)