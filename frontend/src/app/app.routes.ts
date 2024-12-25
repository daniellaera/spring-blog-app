import { Routes } from '@angular/router';
import {PostListComponent} from './post-list/post-list.component';
import {PostDetailComponent} from './post-detail/post-detail.component';

export const routes: Routes = [
  { path: '', component: PostListComponent }, // Default route for the post list
  { path: 'post/:postId', component: PostDetailComponent }, // Route for individual post details
];
