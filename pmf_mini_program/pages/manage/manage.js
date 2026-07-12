// 管理页面 - 人员/仓库/货物 CRUD
const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
  data: {
    activeTab: 'personnel',
    searchKeyword: '',
    searchPlaceholder: '搜索人员...',

    // 数据列表
    personnelList: [],
    warehouseList: [],
    goodsList: [],
    filteredList: [],

    // 模态框
    showModal: false,
    modalTitle: '',
    isEdit: false,
    editId: null,

    // 表单
    form: {},
    sizeOptions: ['大', '中', '小'],
    sizeIndex: 0
  },

  onLoad() {
    this.setData({
      searchPlaceholder: '搜索人员...'
    })
  },

  onShow() {
    if (getApp().checkLogin()) {
      this.loadData()
    }
  },

  async loadData() {
    util.showLoading('加载中...')
    try {
      const [personnel, warehouses, goods] = await Promise.all([
        api.getUserList().catch(() => ({ data: [] })),
        api.getWarehouseList().catch(() => ({ data: [] })),
        api.getGoodsList().catch(() => ({ data: [] }))
      ])

      this.setData({
        personnelList: personnel.data || [],
        warehouseList: warehouses.data || [],
        goodsList: goods.data || []
      })

      this.applyFilter()
    } catch (err) {
      console.error('加载数据失败', err)
    } finally {
      util.hideLoading()
    }
  },

  // Tab切换
  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    const placeholders = {
      personnel: '搜索人员...',
      warehouse: '搜索仓库...',
      goods: '搜索货物...'
    }
    this.setData({
      activeTab: tab,
      searchKeyword: '',
      searchPlaceholder: placeholders[tab] || '搜索...'
    })
    this.applyFilter()
  },

  // 搜索
  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value })
  },

  doSearch() {
    this.applyFilter()
  },

  applyFilter() {
    const { activeTab, searchKeyword } = this.data
    let list = []

    switch (activeTab) {
      case 'personnel':
        list = this.data.personnelList
        if (searchKeyword) {
          const kw = searchKeyword.toLowerCase()
          list = list.filter(item =>
            (item.username && item.username.toLowerCase().includes(kw)) ||
            (item.real_name && item.real_name.toLowerCase().includes(kw)) ||
            (item.phone && item.phone.includes(kw))
          )
        }
        break
      case 'warehouse':
        list = this.data.warehouseList
        if (searchKeyword) {
          const kw = searchKeyword.toLowerCase()
          list = list.filter(item =>
            (item.warehouse_name && item.warehouse_name.toLowerCase().includes(kw)) ||
            (item.warehouse_type && item.warehouse_type.toLowerCase().includes(kw))
          )
        }
        break
      case 'goods':
        list = this.data.goodsList
        if (searchKeyword) {
          const kw = searchKeyword.toLowerCase()
          list = list.filter(item =>
            (item.goods_name && item.goods_name.toLowerCase().includes(kw)) ||
            (item.goods_code && item.goods_code.toLowerCase().includes(kw)) ||
            (item.category && item.category.toLowerCase().includes(kw))
          )
        }
        break
    }

    this.setData({ filteredList: list })
  },

  // 新增
  openAddModal() {
    this.setData({
      showModal: true,
      modalTitle: this.getModalTitle('add'),
      isEdit: false,
      editId: null,
      form: {},
      sizeIndex: 0
    })
  },

  // 编辑
  openEditModal(e) {
    const item = e.currentTarget.dataset.item
    const form = { ...item }
    // 删除id字段，防止覆盖
    let sizeIndex = 0

    if (this.data.activeTab === 'warehouse' && item.warehouse_size) {
      const sizeMap = { '大': 0, '中': 1, '小': 2 }
      sizeIndex = sizeMap[item.warehouse_size] || 0
    }

    this.setData({
      showModal: true,
      modalTitle: this.getModalTitle('edit'),
      isEdit: true,
      editId: this.getEditId(item),
      form,
      sizeIndex
    })
  },

  getModalTitle(action) {
    const typeMap = { personnel: '人员', warehouse: '仓库', goods: '货物' }
    const type = typeMap[this.data.activeTab] || ''
    return action === 'add' ? '新增' + type : '编辑' + type
  },

  getEditId(item) {
    const { activeTab } = this.data
    if (activeTab === 'personnel') return item.user_id
    if (activeTab === 'warehouse') return item.warehouse_id
    if (activeTab === 'goods') return item.goods_id
    return null
  },

  // 关闭模态框
  closeModal() {
    this.setData({ showModal: false, form: {} })
  },

  noop() {},

  // 表单输入
  onFormInput(e) {
    const field = e.currentTarget.dataset.field
    this.setData({
      ['form.' + field]: e.detail.value
    })
  },

  // 仓库规模选择
  onSizeChange(e) {
    const index = parseInt(e.detail.value)
    const sizeMap = { 0: '大', 1: '中', 2: '小' }
    this.setData({
      sizeIndex: index,
      'form.warehouse_size': sizeMap[index]
    })
  },

  // 提交表单
  async submitForm() {
    const { activeTab, isEdit, editId, form } = this.data

    try {
      util.showLoading('保存中...')

      if (activeTab === 'personnel') {
        if (!form.username || !form.real_name) {
          util.showToast('请填写用户名和真实姓名')
          util.hideLoading()
          return
        }
        if (isEdit) {
          form.user_id = editId
          await api.updateUser(form)
        } else {
          if (!form.password) {
            util.showToast('请输入密码')
            util.hideLoading()
            return
          }
          await api.createUser(form)
        }
      } else if (activeTab === 'warehouse') {
        if (!form.warehouse_name) {
          util.showToast('请输入仓库名称')
          util.hideLoading()
          return
        }
        if (isEdit) {
          form.warehouse_id = editId
          await api.updateWarehouse(form)
        } else {
          await api.createWarehouse(form)
        }
      } else if (activeTab === 'goods') {
        if (!form.goods_code || !form.goods_name) {
          util.showToast('请填写货物编码和名称')
          util.hideLoading()
          return
        }
        if (isEdit) {
          form.goods_id = editId
          await api.updateGoods(form)
        } else {
          await api.createGoods(form)
        }
      }

      util.hideLoading()
      util.showToast(isEdit ? '修改成功' : '新增成功', 'success')
      this.closeModal()
      this.loadData()
    } catch (err) {
      util.hideLoading()
      console.error('保存失败', err)
    }
  },

  // 删除确认
  async confirmDelete(e) {
    const id = e.currentTarget.dataset.id
    const type = e.currentTarget.dataset.type

    const confirmed = await util.showConfirm('确认删除', '确定要删除该记录吗？此操作不可撤销。')
    if (!confirmed) return

    try {
      util.showLoading('删除中...')

      if (type === 'personnel') {
        await api.deleteUser(id)
      } else if (type === 'warehouse') {
        await api.deleteWarehouse(id)
      } else if (type === 'goods') {
        await api.deleteGoods(id)
      }

      util.hideLoading()
      util.showToast('删除成功', 'success')
      this.loadData()
    } catch (err) {
      util.hideLoading()
      console.error('删除失败', err)
    }
  },

  // 查看仓库详情（库区/库位）
  viewWarehouseDetail(e) {
    const item = e.currentTarget.dataset.item
    wx.showModal({
      title: '仓库详情: ' + item.warehouse_name,
      content: '类型: ' + (item.warehouse_type || '-') +
        '\n规模: ' + (item.warehouse_size || '-') + '型' +
        '\n总库位: ' + (item.total_slots || 0) +
        '\n可用库位: ' + (item.available_slots || 0) +
        '\n位置: ' + (item.location || '-') +
        '\n状态: ' + (item.status || '-') +
        '\n描述: ' + (item.description || '-'),
      showCancel: false,
      confirmText: '知道了'
    })
  }
})
