# system-monitor
linux system monitor

基于springboot2的web项目，监控linux服务器的CPU和Memory的状态。

默认监控java有关的进程，可以在页面重新设置，以及页面可以重新设置监控的频率。

数据使用H2内存数据库。如果有需要请自行修改为其它的数据库。

要求被监控的服务器先安装JDK8以上。运行此项目的jar包, 然后用浏览器打开链接 http://{ip}:23456/monitor 即可。

页面由Echarts图表显示。

可以显示实时的监控数据，以及历史数据的查询显示功能。

![显示图表示例](https://raw.githubusercontent.com/liugh1974/system-monitor/main/images/example.png)
