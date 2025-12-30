import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface ChatMessage {
  text: string;
  isUser: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  private apiUrl = "http://localhost:8080/api/rag/v1";

  constructor(private http:HttpClient) { }

  sendMessage(chatId:string, message:string): Observable<string> {
    const params = new HttpParams()
      .set("question", message)
      .set("chatId", chatId);

    return this.http.get(this.apiUrl + "/chat", { params, responseType: 'text' });
  }

  getHistory(chatId: string): Observable<ChatMessage[]> {
    const params = new HttpParams()
    .set("chatId", chatId);
    return this.http.get<ChatMessage[]>(this.apiUrl + "/history", { params });
  }

  uploadFile(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post(this.apiUrl + "/ingest/file", formData, { responseType: 'text' });
  }

  deleteChat(chatId: string): Observable<string> {
    const params = new HttpParams()
      .set("chatId", chatId);

      return this.http.delete(this.apiUrl + "/deleteChat", { params, responseType: 'text' });
  }
}
