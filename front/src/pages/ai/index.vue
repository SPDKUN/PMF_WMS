<template>
  <div class="ai-chat-page">
    <!-- 对话区域 -->
    <div class="chat-messages" ref="chatContainer">
      <div v-if="messages.length === 0" class="chat-welcome">
        <div class="welcome-icon"><img src="/ai_image.png" alt="AI" class="ai-avatar-img" /></div>
        <div class="welcome-text">您好！我是AI助手，有什么仓储管理问题可以帮您？</div>
        <div class="welcome-hint">您可以询问出入库、盘点、质检、批次管理等相关问题</div>
      </div>

      <div v-for="(msg, idx) in messages" :key="idx" class="message-row" :class="msg.role === 'user' ? 'msg-right' : 'msg-left'">
        <!-- AI消息（左侧） -->
        <template v-if="msg.role === 'assistant'">
          <div class="avatar avatar-ai"><img src="/ai_image.png" alt="AI" class="ai-avatar-img" /></div>
          <div class="msg-body">
            <div class="msg-nickname">AI助手</div>
            <div class="msg-bubble msg-bubble-ai">{{ msg.content }}</div>
          </div>
        </template>
        <!-- 用户消息（右侧） -->
        <template v-else>
          <div class="avatar avatar-user">{{ username.charAt(0) }}</div>
          <div class="msg-body msg-body-user">
            <div class="msg-nickname msg-nickname-right">{{ username }}</div>
            <div class="msg-bubble msg-bubble-user">{{ msg.content }}</div>
          </div>
        </template>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="message-row msg-left">
        <div class="avatar avatar-ai"><img src="/ai_image.png" alt="AI" class="ai-avatar-img" /></div>
        <div class="msg-body">
          <div class="msg-nickname">AI助手</div>
          <div class="msg-bubble msg-bubble-ai typing-dots">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部输入区 -->
    <div class="chat-input-bar">
      <div class="input-wrapper">
        <input
          v-model="inputText"
          type="text"
          class="chat-input"
          placeholder="向AI助手提问，例如：查询今日入库单···"
          @keyup.enter="sendMessage"
        />
        <button class="send-btn" @click="sendMessage" :disabled="loading || !inputText.trim()">
          发送
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request.js'

const CHAT_STORAGE_KEY = 'ai_chat_messages_'

export default {
  name: 'AiChatPage',
  data() {
    return {
      messages: [],
      inputText: '',
      loading: false,
      username: '',
      storageKey: ''
    }
  },
  mounted() {
    this.loadUsername()
    this.storageKey = CHAT_STORAGE_KEY + this.username
    this.loadMessages()
    // 如果从主页带过来初始问题，自动发送
    const initialQ = this.$route.query.q
    if (initialQ) {
      // 清除URL参数防止刷新重复发送
      this.$router.replace({ query: {} })
      this.inputText = initialQ
      this.$nextTick(() => this.sendMessage())
    }
  },
  methods: {
    loadUsername() {
      const stored = localStorage.getItem('userInfo')
      if (stored) {
        try {
          const user = JSON.parse(stored)
          this.username = user.username || '用户'
        } catch (e) {
          this.username = '用户'
        }
      } else {
        this.username = '用户'
      }
    },
    loadMessages() {
      try {
        const saved = localStorage.getItem(this.storageKey)
        if (saved) {
          this.messages = JSON.parse(saved)
          this.$nextTick(() => this.scrollToBottom())
        }
      } catch (e) { /* ignore */ }
    },
    saveMessages() {
      try {
        localStorage.setItem(this.storageKey, JSON.stringify(this.messages))
      } catch (e) { /* ignore */ }
    },
    async sendMessage() {
      const text = this.inputText.trim()
      if (!text || this.loading) return

      // 添加用户消息
      this.messages.push({ role: 'user', content: text })
      this.inputText = ''
      this.saveMessages()
      this.$nextTick(() => this.scrollToBottom())

      // 构建历史对话（不含刚添加的这条）
      const history = this.messages.slice(0, -1).map(m => ({
        role: m.role,
        content: m.content
      }))

      this.loading = true
      try {
        const res = await request.post('/ai/chat', {
          question: text,
          history: history
        })
        if (res.code === 200) {
          this.messages.push({ role: 'assistant', content: res.data })
        } else {
          this.messages.push({ role: 'assistant', content: res.msg || 'AI回复失败，请重试' })
        }
      } catch (e) {
        this.messages.push({ role: 'assistant', content: '网络异常，请检查连接后重试' })
      }
      this.loading = false
      this.saveMessages()
      this.$nextTick(() => this.scrollToBottom())
    },
    scrollToBottom() {
      const el = this.$refs.chatContainer
      if (el) {
        el.scrollTop = el.scrollHeight
      }
    }
  }
}
</script>

<style scoped>
.ai-chat-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 140px);
  background: #f5f7fa;
}

/* 对话区域 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-welcome {
  text-align: center;
  padding: 60px 20px;
}
.welcome-icon { font-size: 48px; margin-bottom: 16px; }
.welcome-text { font-size: 16px; color: #303133; margin-bottom: 8px; }
.welcome-hint { font-size: 13px; color: #909399; }

/* 消息行 */
.message-row {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}
.msg-left { flex-direction: row; }
.msg-right { flex-direction: row-reverse; }

/* 头像 */
.avatar {
  width: 38px; height: 38px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; flex-shrink: 0;
}
.avatar-ai { background: #fff; border: 1px solid #dcdfe6; overflow: hidden; }
.ai-avatar-img { width: 100%; height: 100%; object-fit: cover; }
.avatar-user { background: #409EFF; color: #fff; font-size: 14px; font-weight: 600; }

/* 消息体 */
.msg-body { max-width: 65%; display: flex; flex-direction: column; gap: 4px; }
.msg-body-user { align-items: flex-end; }

.msg-nickname { font-size: 12px; color: #909399; padding: 0 4px; }
.msg-nickname-right { text-align: right; }

.msg-bubble {
  padding: 10px 14px; border-radius: 8px; font-size: 14px;
  line-height: 1.7; word-break: break-word; white-space: pre-wrap;
}
.msg-bubble-ai { background: #fff; border: 1px solid #ebeef5; color: #303133; }
.msg-bubble-user { background: #409EFF; color: #fff; }

/* 打字动画 */
.typing-dots { display: flex; gap: 4px; align-items: center; padding: 14px 18px; }
.typing-dots span {
  width: 8px; height: 8px; border-radius: 50%; background: #c0c4cc;
  animation: typing 1.4s infinite ease-in-out both;
}
.typing-dots span:nth-child(1) { animation-delay: 0s; }
.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

/* 底部输入区 */
.chat-input-bar {
  padding: 12px 24px 16px;
  background: #fff;
  border-top: 1px solid #ebeef5;
}
.input-wrapper {
  display: flex; gap: 10px; align-items: center;
}
.chat-input {
  flex: 1; height: 42px; padding: 0 14px;
  border: 1px solid #dcdfe6; border-radius: 8px;
  font-size: 14px; color: #303133; outline: none;
  transition: border-color 0.2s;
}
.chat-input:focus { border-color: #409EFF; }
.chat-input::placeholder { color: #c0c4cc; }

.send-btn {
  padding: 0 24px; height: 42px; border: none; border-radius: 8px;
  background: #409EFF; color: #fff; font-size: 14px;
  cursor: pointer; transition: background 0.2s; white-space: nowrap;
}
.send-btn:hover { background: #66b1ff; }
.send-btn:disabled { background: #a0cfff; cursor: not-allowed; }

@media (max-width: 768px) {
  .chat-messages { padding: 12px; }
  .msg-body { max-width: 80%; }
}
</style>
