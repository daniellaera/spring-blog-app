import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { PostService } from '../../../core/services/post.service';
import { LoginService } from '../../../core/services/login.service';

@Component({
  selector: 'app-post-create',
  standalone: true,
  imports: [
    RouterLink,
    FormsModule,
    ButtonModule,
    InputTextModule,
    TextareaModule,
    ToastModule,
  ],
  providers: [MessageService],
  templateUrl: './post-create.component.html',
  styleUrl: './post-create.component.scss'
})
export class PostCreateComponent {
  private postService = inject(PostService);
  private loginService = inject(LoginService);
  private router = inject(Router);
  private messageService = inject(MessageService);

  title = '';
  content = '';
  titleError = '';
  contentError = '';
  loading = false;

  submit(): void {
    this.titleError = '';
    this.contentError = '';

    if (!this.title.trim()) {
      this.titleError = 'Title is required';
    }
    if (!this.content.trim()) {
      this.contentError = 'Content is required';
    }
    if (this.titleError || this.contentError) {
      return;
    }

    this.loading = true;
    const author = this.loginService.currentUser() || '';

    this.postService.create({ title: this.title.trim(), content: this.content.trim(), author, comments: [] }).subscribe({
      next: post => {
        this.loading = false;
        this.messageService.add({
          severity: 'success',
          summary: 'Published!',
          detail: 'Your post has been published.',
          life: 2000
        });
        setTimeout(() => this.router.navigate(['/posts', post.id]), 1000);
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
