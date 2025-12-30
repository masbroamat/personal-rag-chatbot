import { ChatService } from './services/chat.service';
import { CommonModule } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { Router, RouterModule } from "@angular/router";

@Component({
  selector: "app-root",
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: "./app.component.html",
  styleUrl: "./app.component.scss",
})
export class AppComponent implements OnInit {
  sessions: any[] = [];
  showDeleteModal: boolean = false;
  currentChatId: string = '';

  constructor(private router: Router, private chatService: ChatService) {}

  ngOnInit() {
    this.loadSessions();
    if (this.sessions.length === 0) {
      this.createNewChat();
    }
  }

  createNewChat() {
    const newId = 'chat_' + Date.now();
    const newSession = { id: newId, name: `Chat ${this.sessions.length + 1}` };
    this.sessions.push(newSession);
    this.saveSessions();
    this.router.navigate(['/chat', newId]);
  }

  loadSessions() {
    this.sessions = JSON.parse(localStorage.getItem("masbro_sessions") || "[]");
  }

  saveSessions() {
    localStorage.setItem("masbro_sessions", JSON.stringify(this.sessions));
  }

  deleteChat() {
    if (!this.currentChatId) return;

    const idToDelete = this.currentChatId;

    this.chatService.deleteChat(idToDelete).subscribe({
      next: (response) => {
        console.log("Server delete success:", response);

        this.sessions = this.sessions.filter(s => s.id !== idToDelete);
        
        this.saveSessions();

        if (this.router.url.includes(idToDelete)) {
            if (this.sessions.length > 0) {
               this.router.navigate(['/chat', this.sessions[0].id]);
            } else {
               this.createNewChat();
            }
        }
        this.cancelDelete();
      },
      error: (error) => {
        console.error("Failed to delete on server. Keeping local copy.", error);
        alert("Could not delete chat from server.");
        this.cancelDelete();
      }
    });
  }

  showDeletePopup(chatId: string){
    this.showDeleteModal = true;
    this.currentChatId = chatId;
  }
  
  cancelDelete(){
    this.showDeleteModal = false;
    this.currentChatId = "";
  }
}