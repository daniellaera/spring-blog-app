import {Component, OnInit} from '@angular/core';
import {PostService} from '../post.service';
import {NgForOf} from '@angular/common';
import {CardModule} from 'primeng/card';
import {ButtonDirective} from 'primeng/button';

interface Post {
  title: string;
  content: string;
}

@Component({
  selector: 'app-post-list',
  standalone: true,
  imports: [
    NgForOf,
    CardModule,
    ButtonDirective
  ],
  templateUrl: './post-list.component.html',
  styleUrl: './post-list.component.scss'
})
export class PostListComponent implements OnInit {
  posts: Post[] = [];  // Initialize an empty array for posts

  constructor(private postService: PostService) { }

  ngOnInit(): void {
    // Fetch the posts when the component is initialized
    this.postService.getPosts().subscribe({
      next: (data) => {
        this.posts = data;  // Set the posts data from the API
      },
      error: (err) => {
        console.error('Error fetching posts:', err);
      }
    });
  }

  onReadMore(post: Post): void {
    console.log('Read more about: ', post);
    // You can navigate to a post detail page or show a modal with more information.
  }
}
