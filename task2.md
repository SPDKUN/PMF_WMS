小程序报错如下，请帮我修正问题，并教我怎么正常运行小程序(需要打开什么东西)


[自动热重载] 已开启代码文件保存后自动热重载
[system] WeChatLib: 3.16.2 (2026.7.10 10:28:05)
[system] Subpackages: N/A
[system] LazyCodeLoading: true
Lazy code loading is enabled. Only injecting required components.
[ WXML 文件编译错误] ./pages/query/query.wxml
Bad attr `wx:else` with message: `wx:if not found, then something must be wrong`.
  24 |       <view>暂无数据</view>
  25 |     </view>

> 26 |     <view wx:else class="card" wx:for="{{displayList}}" wx:key="user_id">
> |     ^
> 27 |       <view class="detail-row"><text class="detail-label">用户名:</text> {{item.username}}</view>
> 28 |       <view class="detail-row"><text class="detail-label">姓名:</text> {{item.real_name}}</view>
> 29 |       <view class="detail-row"><text class="detail-label">手机号:</text> {{item.phone || '-'}}</view>
> at files://pages/query/query.wxml#26(env: Windows,mp,2.01.2510290; lib: 3.16.2)
> Sun Jul 12 2026 16:34:06 GMT+0800 (中国标准时间) 文章推荐
> getSystemInfo API 提示
> 小程序基础库从 3.7.0 起正式支持 HarmonyOS 平台，开发者可通过 wx.getDeviceInfo() 判断平台进行兼容处理，让小程序在 HarmonyOS 也获得最佳体验，查看指引[https://developers.weixin.qq.com/community/develop/doc/00008e041106f0259bb33530164409]
> 关于上述警告，点击查看更多信息：https://developers.weixin.qq.com/community/develop/doc/00008e041106f0259bb33530164409]
> [ WXML 文件编译错误] ./pages/query/query.wxml
> Bad attr `wx:else` with message: `wx:if not found, then something must be wrong`.
> 24 |       <view>暂无数据</view>
> 25 |     </view>
> 26 |     <view wx:else class="card" wx:for="{{displayList}}" wx:key="user_id">
> |     ^
> 27 |       <view class="detail-row"><text class="detail-label">用户名:</text> {{item.username}}</view>
> 28 |       <view class="detail-row"><text class="detail-label">姓名:</text> {{item.real_name}}</view>
> 29 |       <view class="detail-row"><text class="detail-label">手机号:</text> {{item.phone || '-'}}</view>
> at files://pages/query/query.wxml#26(env: Windows,mp,2.01.2510290; lib: 3.16.2)
> [渲染层错误] ReferenceError: SystemError (webviewScriptError)
> __route__ is not defined(env: Windows,mp,2.01.2510290; lib: 3.16.2)