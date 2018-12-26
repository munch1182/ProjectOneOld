- libNative：只保留V4、V7、V11等官方包，
- libExpand：引入一般依赖及基本实现
            :按需引入

- libApp： 放置图片、asset等资源文件
          根据项目实例化的配置文件
          根据项目需要的其它引入依赖
          Application类
          业务的基础资源
          通用控件、styles等
            :被所有module依赖

- app：壳，一般来说只用于设置启动页，以及最后的汇总打包
            :依赖所有module

- 最后效果：libNative和libExpand可以被打包成依赖
           libApp可以根据不同项目直接复制修改
           业务Module各自打包运行


- [Android性能优化（一）之启动加速](https://www.jianshu.com/p/f5514b1a826c)
- [Android之点击Home键后再次打开导致APP重启问题](https://blog.csdn.net/LVXIANGAN/article/details/82870762?tdsourcetag=s_pctim_aiomsg)
- [Google官方推出的Android架构组件系列文章（一）App架构指南](https://www.jianshu.com/p/fe509262a1f7)

