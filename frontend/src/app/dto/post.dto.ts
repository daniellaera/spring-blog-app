import {CommentDTO} from './comment.dto';

export interface PostDTO {
  id?: number;           // The unique identifier for the post (typically comes from a database)
  title: string;        // The title of the post
  content: string;      // The content of the post (body of the post)
  createdAt?: string;    // The timestamp when the post was created
  updatedAt?: string;   // The timestamp when the post was last updated (optional)
  comments: CommentDTO[];
}
