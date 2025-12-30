import { Component, OnInit, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MarkdownComponent } from 'ngx-markdown';
import { ActivatedRoute } from '@angular/router';
import { ChatService } from '../../services/chat.service';

interface ChatMessage { text: string; isUser: boolean; }

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, MarkdownComponent],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss']
})
export class ChatComponent implements OnInit, AfterViewChecked {
  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  chatId: string = '';
  messages: ChatMessage[] = [];
  userInput: string = '';
  isLoading: boolean = false;
  currentSession: any = {}; 

  constructor(
    private route: ActivatedRoute,
    private chatService: ChatService
  ) {}

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.chatId = id;
        this.loadChat(id);
      }
    });
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  scrollToBottom(): void {
    try {
      this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
    } catch(err) { }
  }

  loadChat(id: string) {
    this.messages = [];
    const sessions = JSON.parse(localStorage.getItem('masbro_sessions') || '[]');
    this.currentSession = sessions.find((s: any) => s.id === id) || { id: id };

    this.chatService.getHistory(id).subscribe({
      next: (history) => {
        this.messages = history;
        if (this.messages.length === 0) {
            this.messages.push({ text: "Hello! New conversation started.", isUser: false });
        }
        this.mergeLocalState();
      },
      error: () => this.messages.push({ text: "Error loading history.", isUser: false })
    });
  }

  sendMessage() {
    if (!this.userInput.trim()) return;
    const text = this.userInput;

    this.messages.push({ text, isUser: true });
    this.userInput = '';
    
    this.isLoading = true;
    this.updateSessionState(true, text);

    this.chatService.sendMessage(this.chatId, text).subscribe({
      next: (response) => {
        if (this.chatId === this.currentSession.id) {
           this.messages.push({ text: response, isUser: false });
        }
        this.isLoading = false;
        this.updateSessionState(false, undefined);
      },
      error: () => {
        this.isLoading = false;
        this.updateSessionState(false, undefined);
      }
    });
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.isLoading = true;
      this.chatService.uploadFile(file).subscribe({
        next: (response) => {
          const msg = `📁 System: ${response}`;
          this.messages.push({ text: msg, isUser: false });
          this.saveSystemLog(msg);
          this.isLoading = false;
        },
        error: () => {
          this.isLoading = false;
          this.messages.push({ text: "❌ Upload failed", isUser: false });
        }
      });
    }
  }
  
  updateSessionState(isLoading: boolean, pendingInput?: string) {
    const sessions = JSON.parse(localStorage.getItem('masbro_sessions') || '[]');
    const index = sessions.findIndex((s: any) => s.id === this.chatId);
    if (index !== -1) {
      sessions[index].isLoading = isLoading;
      sessions[index].pendingInput = pendingInput;
      localStorage.setItem('masbro_sessions', JSON.stringify(sessions));
      this.currentSession = sessions[index];
    }
  }

  saveSystemLog(text: string) {
    const sessions = JSON.parse(localStorage.getItem('masbro_sessions') || '[]');
    const index = sessions.findIndex((s: any) => s.id === this.chatId);
    if (index !== -1) {
      if (!sessions[index].systemLogs) sessions[index].systemLogs = [];
      sessions[index].systemLogs.push({ text, insertAfterIndex: this.messages.length });
      localStorage.setItem('masbro_sessions', JSON.stringify(sessions));
    }
  }

  mergeLocalState() {
     if (this.currentSession.systemLogs) {
        this.currentSession.systemLogs.forEach((log: any) => {
             if (log.insertAfterIndex <= this.messages.length) {
                this.messages.splice(log.insertAfterIndex, 0, { text: log.text, isUser: false });
             } else {
                this.messages.push({ text: log.text, isUser: false });
             }
        });
     }
     if (this.currentSession.isLoading && this.currentSession.pendingInput) {
        const lastMsg = this.messages[this.messages.length - 1];
        if (!lastMsg || lastMsg.text !== this.currentSession.pendingInput) {
             this.messages.push({ text: this.currentSession.pendingInput, isUser: true });
             this.isLoading = true;
        }
     }
  }
}