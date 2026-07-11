import axios from 'axios'

// import { useUserStore } from '../store/modules/user'


// pinia的user仓库
// const userStore = useUserStore();



// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL, // 基础路径（从环境变量获取）
  timeout: 10000, // 请求超时时间（10秒）
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})



// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 添加token到请求头
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
 
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use((response) => {
  if (response.config.responseType === "blob") {
    return response;
  }
  return response.data
})

// 封装请求方法（简化调用）
const request = {
  get(url, params = {}, config = {}) {
    return service.get(url, { params, ...config })
  },
  post(url, data = {}, config = {}) {
    return service.post(url, data, config)
  },
  put(url, data = {}, config = {}) {
    return service.put(url, data, config)
  },
  delete(url, params = {}, config = {}) {
    return service.delete(url, { params, ...config })
  },
  upload(url, file, params = {}, config = {}) {
    // 文件上传封装
    const formData = new FormData()
    formData.append('file', file)
    // 添加其他参数
    Object.keys(params).forEach(key => {
      formData.append(key, params[key])
    })
    return service.post(url, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      ...config
    })
  },
  async exportExcel(url, fileName = "导出文件.xlsx") {
  try {
    const res = await service.get(url, {
      responseType: "blob", // 关键
    });

    const blob = new Blob([res.data], {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });

    const downloadUrl = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = downloadUrl;
    a.download = fileName;
    a.click();
    URL.revokeObjectURL(downloadUrl);
  } catch (err) {
    console.error("导出失败", err);
  }
}
}

export default request