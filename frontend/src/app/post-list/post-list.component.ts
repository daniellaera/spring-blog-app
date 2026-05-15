import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PostService } from '../post.service';
import { LoginService, UserSession } from '../login.service';
import { PostFormComponent } from '../post-form/post-form.component';
import { DatePipe } from '@angular/common';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { AvatarModule } from 'primeng/avatar';
import { DialogModule } from 'primeng/dialog';
import { SkeletonModule } from 'primeng/skeleton';
import { PostDTO } from '../dto/post.dto';

@Component({
  selector: 'app-post-list',
  standalone: true,
  imports: [
    RouterLink,
    DatePipe,
    CardModule,
    PostFormComponent,
    DialogModule,
    SkeletonModule,
    ButtonModule,
    AvatarModule,
  ],
  templateUrl: './post-list.component.html',
  styleUrls: ['./post-list.component.scss']
})
export class PostListComponent implements OnInit {
  posts: PostDTO[] = [];
  loading = false;
  showModal = false;
  session: UserSession | null = null;
  sessionLoading = true;

  constructor(
    private postService: PostService,
    private loginService: LoginService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loginService.getSession().subscribe({
      next: s => {
        this.session = s;
        this.sessionLoading = false;
        this.route.queryParams.subscribe(params => {
          if (params['newPost'] === 'true') {
            this.showModal = true;
            this.router.navigate([], { queryParams: {}, replaceUrl: true });
          }
        });
      },
      error: () => {
        this.session = null;
        this.sessionLoading = false;
      }
    });

    this.loadPosts();
  }

  loadPosts(): void {
    this.loading = true;
    this.postService.getPosts().subscribe({
      next: data => {
        this.posts = data.map(post => ({ ...post, comments: post.comments || [] }));
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  onPostAdded(newPost: PostDTO): void {
    this.posts.unshift(newPost);
    this.showModal = false;
  }

  onModalClose(): void {
    this.loadPosts();
  }

  get username(): string {
    return this.session?.username || '';
  }

  getInitial(title: string): string {
    return title?.charAt(0).toUpperCase() || 'P';
  }
}
