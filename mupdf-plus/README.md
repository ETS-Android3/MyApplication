# 项目目录导视图

## Swiper Adapter

 - BaseAdapter
 -    ⬇️(extends)
 - MuPDFPageAdapter
 -    ⬇️
 - ImageView

## ImageView

 - OpaqueImageView(mEntire)
 -    ⬇️
 - ViewGroup
 -    ⬇️
 - PageView
 -    ⬇️
 - MuPDFPageView


## ReaderView

 - ReaderView (为各个Adapter处理手势事件)
 -    ⬇️
 - MuPDFReaderView
 -    ⬇️
 - Activity - createUI(new MuPDFReaderView)