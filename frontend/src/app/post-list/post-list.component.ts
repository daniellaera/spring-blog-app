import { Component, OnInit } from '@angular/core';
import { PostService } from '../post.service';
import { PostFormComponent } from '../post-form/post-form.component';
import {NgForOf, NgIf} from '@angular/common';
import { CardModule } from 'primeng/card';
import {PostDTO} from '../dto/post.dto';
import {RouterLink} from '@angular/router';
import {DialogModule} from 'primeng/dialog';
import {ProgressSpinnerModule} from 'primeng/progressspinner';
import {BadgeModule} from 'primeng/badge';

@Component({
  selector: 'app-post-list',
  standalone: true,
  imports: [
    NgForOf,
    CardModule,
    PostFormComponent,
    NgIf,
    RouterLink,
    DialogModule,
    ProgressSpinnerModule,
    BadgeModule,
  ],
  templateUrl: './post-list.component.html',
  styleUrls: ['./post-list.component.scss']
})
export class PostListComponent implements OnInit {
  posts: PostDTO[] = [];
  loading: boolean = false;
  showModal: boolean = false;

  constructor(private postService: PostService) {}

  ngOnInit(): void {
    this.loadPosts();
  }

  loadPosts(): void {
    this.loading = true;
    this.postService.getPosts().subscribe({
      next: (data) => {
        this.posts = data.map(post => ({
          ...post,
          comments: post.comments || []
        }));
      },
      complete: () => {
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching posts:', err);
        this.loading = false;
      },
    });
  }

  onPostAdded(newPost: PostDTO): void {
    this.posts.unshift(newPost); // Add new post at the top of the list
    this.showModal = false;
  }

  onModalClose(): void {
    this.loadPosts(); // Reload posts after modal closes
  }
}
