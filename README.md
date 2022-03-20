# LZWAsyncLayoutInflater
Android性能优化——Android异步渲染View

 * <p>【官方AsyncLayoutInflater缺点】：</p>
 * <p> 1、所构建的View中不能直接使用Handler或者调用Looper.myLooper()，因为异步线程默认没有调用Looper.prepare()；</p>
 * <p> 2、异步转换出来的View并没有被加到parent中，AsyncLayoutInflater是调用了LayoutInflater.inflate(int, ViewGroup, false)，
 * 因此如果需要添加到parent View中，就需要我们自己手动添加；</p>
 * <p> 3、AsyncLayoutInflater不支持设置LayoutInflater.Factory或者LayoutInflater.Factory2；</p>
 * <p> 4、同时缓存队列默认10的大小限制如果超过了10个则会导致主线程的等待；</p>
 * <p> 5、使用单线程来做全部的inflate工作，如果一个界面中layout很多不一定能满足需求。</p>
 * <p>6、不支持fragment</p>
 *
 * <p>改造后：</p>
 * <p>【解决缺点2】1.异步转换出来的View自动将其添加到parent中</p>
 * <p>【解决缺点3】2.支持设置LayoutInflater.Factory或者LayoutInflater.Factory2</p>
 * <p>【解决缺点4和缺点5】3.引入线程池，减少单线程等待。</p>
 * <p>4.layoutinflater布局的时候，可以统计该view加载时的耗时</p>
